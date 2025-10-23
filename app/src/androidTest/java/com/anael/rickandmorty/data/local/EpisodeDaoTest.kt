package com.anael.rickandmorty.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EpisodeDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: EpisodeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Only allowed in tests
            .build()

        dao = database.episodeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsertAll inserts and replaces correctly`() = runBlocking {
        val episodes = listOf(
            EpisodeEntity(1, "Pilot", "Dec 2", "S01E01", listOf("Rick"), "", ""),
            EpisodeEntity(2, "Lawnmower Dog", "Dec 9", "S01E02", listOf("Morty"), "", "")
        )

        dao.upsertAll(episodes)

        // Replace episode 1 with updated name:
        val updated = listOf(
            EpisodeEntity(1, "Pilot Updated", "Dec 2", "S01E01", listOf("Rick"), "", ""),
        )
        dao.upsertAll(updated)

        val pagingSource = dao.pagingSource().load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        val items = (pagingSource as androidx.paging.PagingSource.LoadResult.Page).data
        assertEquals(2, items.size)
        assertTrue(items.any { it.name == "Pilot Updated" })
    }

    @Test
    fun `clearAll removes everything`() = runBlocking {
        dao.upsertAll(
            listOf(
                EpisodeEntity(1, "Ep1", "A", "S01E01", emptyList(), "", ""),
                EpisodeEntity(2, "Ep2", "B", "S01E02", emptyList(), "", "")
            )
        )

        dao.clearAll()

        val result = dao.pagingSource().load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as androidx.paging.PagingSource.LoadResult.Page

        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `deleteById removes the right episode`() = runBlocking {
        dao.upsertAll(
            listOf(
                EpisodeEntity(1, "Ep1", "A", "S01E01", emptyList(), "", ""),
                EpisodeEntity(2, "Ep2", "B", "S01E02", emptyList(), "", "")
            )
        )

        dao.deleteById(1)

        val result = dao.pagingSource().load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as androidx.paging.PagingSource.LoadResult.Page

        assertEquals(1, result.data.size)
        assertEquals(2, result.data.first().id)
    }

    @Test
    fun `insertNoop inserts only if id does not exist`() = runBlocking {
        val episode = EpisodeEntity(100, "No-op", "-", "S00E00", emptyList(), "", "")

        dao.insertNoop(episode)  // first insert should work
        dao.insertNoop(episode)  // second insert  should be ignored (OnConflict IGNORE)

        val result = dao.pagingSource().load(
            androidx.paging.PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as androidx.paging.PagingSource.LoadResult.Page

        assertEquals(1, result.data.size)
        assertEquals(100, result.data.first().id)
    }
}
