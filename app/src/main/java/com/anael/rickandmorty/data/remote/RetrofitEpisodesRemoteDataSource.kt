package com.anael.rickandmorty.data.remote

import javax.inject.Inject

class RetrofitEpisodesRemoteDataSource @Inject constructor(
    private val api: RnMApiService
) : EpisodesRemoteDataSource {

    override suspend fun getEpisodesPage(page: Int) = api.getEpisodesPage(page)
    override suspend fun getEpisodeById(id: String) = api.getEpisodeById(id)
    override suspend fun getCharacterById(id: String) = api.getCharacterById(id)
    override suspend fun getCharactersByIds(idsCsv: String) = api.getCharactersByIds(idsCsv)
}
