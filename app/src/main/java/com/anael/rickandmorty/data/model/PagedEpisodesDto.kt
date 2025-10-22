package com.anael.rickandmorty.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class that represents an episodes search paginated response from EpisodesService.
 */
data class PagedEpisodesDto(
    @field:SerializedName("info") val info: PageInfo,
    @field:SerializedName("results") val results: List<EpisodeDto>
)

data class PageInfo(
    @field:SerializedName("count") val count: Int,
    @field:SerializedName("pages") val pages: Int,
    @field:SerializedName("next") val next: String?,   // full URL or null
    @field:SerializedName("prev") val prev: String?    // full URL or null
)