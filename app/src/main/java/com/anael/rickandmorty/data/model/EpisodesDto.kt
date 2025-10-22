package com.anael.rickandmorty.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class that represents an Episode from EpisodesService.
 */
data class EpisodeDto(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("air_date") val airingDate: String,
    @field:SerializedName("episode") val episode: String,
    @field:SerializedName("characters") val characters: List<String>,
    @field:SerializedName("url") val url: String,
    @field:SerializedName("created") val created: String,
)