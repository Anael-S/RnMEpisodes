package com.anael.rickandmorty.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

// RemoteKeys.kt — track pagination cursors for RemoteMediator
@Entity(tableName = "episode_remote_keys")
data class EpisodeRemoteKey(
    @PrimaryKey val episodeId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)

@Dao
interface EpisodeRemoteKeyDao {
    @Query("SELECT * FROM episode_remote_keys WHERE episodeId = :id")
    suspend fun remoteKeyById(id: Int): EpisodeRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(keys: List<EpisodeRemoteKey>)

    @Query("DELETE FROM episode_remote_keys")
    suspend fun clearRemoteKeys()
}
