package com.anael.rickandmorty.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class CharacterRnM(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val origin: Origin,
    val image: String,
    val episode: ImmutableList<String>,
)

@Immutable
data class Origin(
    val name: String,
    val url: String
)