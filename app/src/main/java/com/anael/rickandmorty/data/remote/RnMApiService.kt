package com.anael.rickandmorty.data.remote

import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Used to connect to the R&M API to fetch episodes and characters
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

    companion object {
        private const val BASE_URL = "https://rickandmortyapi.com/api/"

        fun create(): RnMApiService {
            val logger = HttpLoggingInterceptor().apply { level = Level.BASIC }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RnMApiService::class.java)
        }
    }
}