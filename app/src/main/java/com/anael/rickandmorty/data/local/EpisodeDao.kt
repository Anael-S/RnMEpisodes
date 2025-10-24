package com.anael.rickandmorty.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO  for episode to store them in the DB
 */
@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EpisodeEntity>)

    @Query("DELETE FROM episodes")
    suspend fun clearAll()

    // --- Poke helpers (invalidate PagingSource without changing visible data -> avoid flickering) ---
    //This is mainly used as a “poke” mechanism for invalidating the PagingSource without actually changing any visible data in the database.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoop(noop: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
