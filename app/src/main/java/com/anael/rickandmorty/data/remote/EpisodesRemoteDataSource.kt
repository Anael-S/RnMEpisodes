package com.anael.rickandmorty.data.remote

import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto

interface EpisodesRemoteDataSource {
    suspend fun getEpisodesPage(page: Int): PagedEpisodesDto
    suspend fun getEpisodeById(id: String): EpisodeDto
    suspend fun getCharacterById(id: String): CharacterDto
    suspend fun getCharactersByIds(idsCsv: String): List<CharacterDto>
}
