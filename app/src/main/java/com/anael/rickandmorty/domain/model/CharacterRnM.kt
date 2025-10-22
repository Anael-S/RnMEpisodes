package com.anael.rickandmorty.domain.model

data class CharacterRnM(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val origin: Origin,
    val image: String,
    val episode: List<String>,
)

data class Origin(
    val name: String,
    val url: String
)