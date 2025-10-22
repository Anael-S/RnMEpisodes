package com.anael.rickandmorty.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EpisodeEntity>)

    @Query("DELETE FROM episodes")
    suspend fun clearAll()

    // --- Poke helpers (invalidate PagingSource without changing visible data -> avoid flickering) ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoop(noop: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
