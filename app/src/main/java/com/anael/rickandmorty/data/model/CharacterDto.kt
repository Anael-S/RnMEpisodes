package com.anael.rickandmorty.data.model

import com.google.gson.annotations.SerializedName

data class CharacterDto(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("status") val status: String,
    @field:SerializedName("species") val species: String,
    @field:SerializedName("type") val type: String,
    @field:SerializedName("gender") val gender: String,
    @field:SerializedName("origin") val origin: OriginDto,
    @field:SerializedName("location") val location: LocationDto,
    @field:SerializedName("image") val image: String,
    @field:SerializedName("episode") val episode: List<String>,
    @field:SerializedName("url") val url: String,
    @field:SerializedName("created") val created: String
)

data class OriginDto(
    @field:SerializedName("name") val name: String,
    @field:SerializedName("url") val url: String
)

data class LocationDto(
    @field:SerializedName("name") val name: String,
    @field:SerializedName("url") val url: String
)
