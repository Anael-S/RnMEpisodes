package com.anael.rickandmorty.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_refresh")
data class LastRefreshEntity(
    @PrimaryKey val id: Int = 0,  // always a single row
    val timestamp: Long           // epoch millis
)
