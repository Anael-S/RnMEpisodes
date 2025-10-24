package com.anael.rickandmorty.data.remote

import javax.inject.Inject

/**
 * DataSource for the RnMApi
 */
class RetrofitRnMApiRemoteDataSource @Inject constructor(
    private val api: RnMApiService
) : RnMApiRemoteDataSource {

    override suspend fun getEpisodesPage(page: Int) = api.getEpisodesPage(page)
    override suspend fun getEpisodeById(id: String) = api.getEpisodeById(id)
    override suspend fun getCharacterById(id: String) = api.getCharacterById(id)
    override suspend fun getCharactersByIds(idsCsv: String) = api.getCharactersByIds(idsCsv)
}
