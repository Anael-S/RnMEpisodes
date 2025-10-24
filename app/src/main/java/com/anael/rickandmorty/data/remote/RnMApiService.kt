package com.anael.rickandmorty.data.remote

import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Service used to connect to the R&M API to fetch episodes and characters
 */
interface RnMApiService {

    @GET("episode")
    suspend fun getEpisodesPage(
        @Query("page") page: Int,
    ): PagedEpisodesDto

    @GET("character/{id}")
    suspend fun getCharacterById(@Path("id") id: String): CharacterDto

    @GET("character/{ids}")
    suspend fun getCharactersByIds(@Path("ids") idsCsv: String): List<CharacterDto>

    @GET("episode/{id}")
    suspend fun getEpisodeById(@Path("id") id: String): EpisodeDto
}