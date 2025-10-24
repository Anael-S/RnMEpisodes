package com.anael.rickandmorty.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Model for episode to store them in the DB
 */
@Entity(tableName = "episodes")
@TypeConverters(StringListConverter::class)
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val airDate: String,
    val episodeCode: String,
    val characters: List<String>,
    val url: String,
    val created: String
)
