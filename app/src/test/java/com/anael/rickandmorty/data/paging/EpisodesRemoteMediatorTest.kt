package com.anael.rickandmorty.data.paging

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.room.Room
import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.local.EpisodeEntity
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PageInfoDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import androidx.paging.PagingSource


/**
 * Integration-like test:
 *  - real in-memory Room DB
 *  - fake RemoteDataSource returning 2 pages
 *  - assert mediator writes episodes, keys and last-refresh
 */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalPagingApi::class)
class EpisodesRemoteMediatorTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var fakeRemote: FakeRemote
    private lateinit var mediator: EpisodesRemoteMediator

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // OK for tests
            .build()

        // Two pages: [1,2] then [3]
        fakeRemote = FakeRemote(
            pages = listOf(
                listOf(dto(1, "Pilot"), dto(2, "Lawnmower Dog")),
                listOf(dto(3, "Anatomy Park"))
            )
        )
        mediator = EpisodesRemoteMediator(remote = fakeRemote, db = db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `refresh inserts first page, keys and last refresh`() = runTest {
        val state = emptyPagingState()

        val result = mediator.load(LoadType.REFRESH, state)
        assertThat((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            .isFalse()

        val all = loadAllEntities()
        assertThat(all.map { it.id }).containsExactly(1, 2)

        // Keys exist with next=2, prev=null (page 1)
        val key1 = db.episodeRemoteKeyDao().remoteKeyById(1)
        assertThat(key1?.prevKey).isNull()
        assertThat(key1?.nextKey).isEqualTo(2)

        // Last refresh written
        val ts = db.lastRefreshDao().getTimestamp()
        assertThat(ts).isNotNull()
    }

    @Test
    fun `append loads next page and finishes at end`() = runTest {
        // First refresh
        mediator.load(LoadType.REFRESH, emptyPagingState())

        // Build a paging state that contains the items currently in DB,
        // so APPEND will look up keys from the last item.
        val firstPageEntities = loadAllEntities()
        val stateForAppend = pagingStateFrom(firstPageEntities)

        val appendResult = mediator.load(LoadType.APPEND, stateForAppend)
        assertThat((appendResult as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            .isTrue() // second page was the end

        val all = loadAllEntities()
        assertThat(all.map { it.id }).containsExactly(1, 2, 3)

        // After last page, nextKey should be null for page 2 keys
        val lastKey = db.episodeRemoteKeyDao().remoteKeyById(3)
        assertThat(lastKey?.nextKey).isNull()
        assertThat(lastKey?.prevKey).isEqualTo(1) // previous page index
    }

    @Test
    fun `prepend ends immediately when there is no previous page`() = runTest {
        mediator.load(LoadType.REFRESH, emptyPagingState())

        // state with current items
        val state = pagingStateFrom(loadAllEntities())

        val prependResult = mediator.load(LoadType.PREPEND, state)
        assertThat((prependResult as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            .isTrue()
    }

    // ---------- helpers ----------

    private fun emptyPagingState(): PagingState<Int, EpisodeEntity> =
        PagingState(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )

    private fun pagingStateFrom(items: List<EpisodeEntity>): PagingState<Int, EpisodeEntity> =
        PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = items,
                    prevKey = null,
                    nextKey = null
                )
            ),
            anchorPosition = items.lastIndex, // near the end
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )

    private fun loadAllEntities(): List<EpisodeEntity> = runBlocking {
        // Use the DAO's PagingSource to read what's in the DB
        val ps = db.episodeDao().pagingSource()
        val page = ps.load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 50,
                placeholdersEnabled = false
            )
        ) as androidx.paging.PagingSource.LoadResult.Page
        page.data
    }

    private fun dto(id: Int, name: String) = EpisodeDto(
        id = id,
        name = name,
        airDate = "December 2, 2013",
        episode = "S01E%02d".format(id),
        characters = emptyList(),
        url = "https://rickandmortyapi.com/api/episode/$id",
        created = "2017-11-10T12:56:33.798Z"
    )

    /**
     * Simple fake that only implements what's needed by the mediator.
     */
    private class FakeRemote(
        private val pages: List<List<EpisodeDto>>
    ) : RnMApiRemoteDataSource {

        override suspend fun getEpisodesPage(page: Int): PagedEpisodesDto {
            require(page >= 1)
            val idx = page - 1
            val results = pages.getOrElse(idx) { emptyList() }
            val next = if (idx + 1 < pages.size) "page=${page + 1}" else null
            return PagedEpisodesDto(
                info = PageInfoDto(
                    count = results.size,
                    pages = pages.size,
                    next = next,
                    prev = if (page > 1) "page=${page - 1}" else null
                ),
                results = results
            )
        }

        // Not used by mediator in these tests
        override suspend fun getEpisodeById(id: String) = error("Not used")
        override suspend fun getCharacterById(id: String) = error("Not used")
        override suspend fun getCharactersByIds(idsCsv: String) = error("Not used")
    }
}
