package com.anael.rickandmorty.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for last refresh timestamp to store it in the DB
 */
@Dao
interface LastRefreshDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LastRefreshEntity)

    @Query("SELECT timestamp FROM last_refresh WHERE id = 0")
    fun getTimestampFlow(): Flow<Long?>

    @Query("SELECT timestamp FROM last_refresh WHERE id = 0")
    suspend fun getTimestamp(): Long?
}
