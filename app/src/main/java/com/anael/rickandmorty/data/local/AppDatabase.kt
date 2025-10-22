package com.anael.rickandmorty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EpisodeEntity::class, EpisodeRemoteKey::class, LastRefreshEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun episodeDao(): EpisodeDao
    abstract fun episodeRemoteKeyDao(): EpisodeRemoteKeyDao
    abstract fun lastRefreshDao(): LastRefreshDao
}
