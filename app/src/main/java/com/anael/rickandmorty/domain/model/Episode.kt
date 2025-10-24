package com.anael.rickandmorty.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * UI classes for Episode
 * Immutable for Compose performance
 */
@Immutable
data class Episode(
    val id: Int,
    val name: String,
    val airDate: String,
    val episodeCode: String,
    val characters: ImmutableList<String>
)
