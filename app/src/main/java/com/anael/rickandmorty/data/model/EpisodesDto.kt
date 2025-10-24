package com.anael.rickandmorty.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO model that represents an Episode from RnMApiService.
 */
data class EpisodeDto(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("air_date") val airDate: String,
    @field:SerializedName("episode") val episode: String,
    @field:SerializedName("characters") val characters: List<String>,
    @field:SerializedName("url") val url: String,
    @field:SerializedName("created") val created: String,
)