package com.anael.rickandmorty.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import androidx.paging.testing.asSnapshot
import com.anael.rickandmorty.data.local.*
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PageInfoDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto
import com.anael.rickandmorty.data.paging.EpisodesRemoteMediator
import com.anael.rickandmorty.data.paging.EpisodesRemoteMediatorFactory
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.testutils.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import androidx.room.withTransaction

class EpisodesRepositoryImplTest {

    // --- mocks ---
    private val db: AppDatabase = mockk(relaxed = true)
    private val episodeDao: EpisodeDao = mockk(relaxed = true)
    private val keyDao: EpisodeRemoteKeyDao = mockk(relaxed = true)
    private val lastDao: LastRefreshDao = mockk(relaxed = true)
    private val mediatorFactory: EpisodesRemoteMediatorFactory = mockk()
    private val mediator: EpisodesRemoteMediator = mockk()
    private val remote: RnMApiRemoteDataSource = mockk()

    // SUT
    private lateinit var repo: EpisodesRepositoryImpl

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        // wire DAOs returned by db
        every { db.episodeDao() } returns episodeDao
        every { db.episodeRemoteKeyDao() } returns keyDao
        every { db.lastRefreshDao() } returns lastDao

        // mediator factory
        every { mediatorFactory.create() } returns mediator

        // lastRefreshFlow
        val flow = MutableSharedFlow<Long?>(replay = 1)
        every { lastDao.getTimestampFlow() } returns flow

        repo = EpisodesRepositoryImpl(db, mediatorFactory, remote)
    }

    @After
    fun tearDown() {
        unmockkAll() // clean any static mocks used inside tests
    }

    // ---- helpers ----
    private fun dtoEpisode(id: Int) = EpisodeDto(
        id = id,
        name = if (id == 1) "Pilot" else "Ep $id",
        airDate = "December 2, 2013",
        episode = "S01E%02d".format(id),
        characters = listOf("https://rickandmortyapi.com/api/character/1"),
        url = "https://rickandmortyapi.com/api/episode/$id",
        created = "2017-11-10T12:56:33.798Z"
    )

    private fun page(items: List<EpisodeDto>, next: Boolean): PagedEpisodesDto {
        val info = PageInfoDto(
            count = items.size,
            pages = if (next) 2 else 1,
            next = if (next) "anything" else null,
            prev = null
        )
        return PagedEpisodesDto(info = info, results = items)
    }

    // ------------------ Tests ------------------

    @Test
    fun `getEpisodeDetail returns mapped domain on success`() = runTest {
        coEvery { remote.getEpisodeById("1") } returns dtoEpisode(1)

        val result = repo.getEpisodeDetail("1")

        assertThat(result.isSuccess).isTrue()
        val ep: Episode = result.getOrThrow()
        assertThat(ep.id).isEqualTo(1)
        assertThat(ep.name).isEqualTo("Pilot")
    }


    @Test
    fun `getEpisodeDetail returns failure when remote throws`() = runTest {
        coEvery { remote.getEpisodeById("1") } throws IOException("500")

        val result = repo.getEpisodeDetail("1")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `syncEpisodes fetches pages, upserts rows and keys, writes last refresh once`() = runTest {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { db.withTransaction<Any?>(any()) } coAnswers {
            val block = arg<suspend () -> Any?>(1)
            block.invoke()
        }

        // Empty results = no mapper invoked = no parse errors possible, the mappers are not tested there
        coEvery { remote.getEpisodesPage(1) } returns page(emptyList(), next = true)
        coEvery { remote.getEpisodesPage(2) } returns page(emptyList(), next = false)

        coEvery { episodeDao.upsertAll(any()) } just Runs
        coEvery { keyDao.upsertAll(any()) } just Runs
        coEvery { lastDao.upsert(any()) } just Runs

        val result = repo.syncEpisodes()
        result.exceptionOrNull()?.let { throw AssertionError("syncEpisodes failed", it) }
        assertThat(result.isSuccess).isTrue()

        // Still called once per page, even if payloads are empty
        coVerify(exactly = 2) { episodeDao.upsertAll(any()) }
        coVerify(exactly = 2) { keyDao.upsertAll(any()) }
        coVerify(exactly = 1) { lastDao.upsert(any<LastRefreshEntity>()) }
    }


    @Test
    fun `syncEpisodes propagates failure when remote fails mid-run`() = runTest {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { db.withTransaction<Any?>(any()) } coAnswers {
            val block = arg<suspend () -> Any?>(1)
            block.invoke()
        }

        coEvery { remote.getEpisodesPage(1) } returns page(listOf(dtoEpisode(1)), next = true)
        coEvery { remote.getEpisodesPage(2) } throws IOException("500")

        val result = repo.syncEpisodes()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun `getEpisodesStream emits mapped items (smoke test)`() = runTest {
        // this prevent Paging from calling into real android.util.Log
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), String()) } returns 0
        every { Log.w(any(), Throwable()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0

        try {
            val entities = listOf(
                EpisodeEntity(1, "Pilot", "Dec 2", "S01E01", listOf("Rick"), "", ""),
                EpisodeEntity(2, "Lawnmower Dog", "Dec 9", "S01E02", listOf("Morty"), "", "")
            )
            val pagingSource = object : androidx.paging.PagingSource<Int, EpisodeEntity>() {
                override fun getRefreshKey(state: androidx.paging.PagingState<Int, EpisodeEntity>) =
                    null

                override suspend fun load(
                    params: LoadParams<Int>
                ): LoadResult<Int, EpisodeEntity> {
                    return LoadResult.Page<Int, EpisodeEntity>(
                        data = entities,
                        prevKey = null,
                        nextKey = null,
                        itemsBefore = 0,
                        itemsAfter = 0
                    )
                }
            }
            every { episodeDao.pagingSource() } returns pagingSource

            every { mediatorFactory.create() } returns mediator
            coEvery { mediator.initialize() } returns RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH
            coEvery { mediator.load(any(), any()) } returns RemoteMediator.MediatorResult.Success(
                true
            )

            val list: List<Episode> = repo.getEpisodesStream().asSnapshot()
            assertThat(list.map { it.id }).containsExactly(1, 2)
            assertThat(list.map { it.name }).containsExactly("Pilot", "Lawnmower Dog")
        } finally {
            unmockkStatic(Log::class)
        }
    }
}
