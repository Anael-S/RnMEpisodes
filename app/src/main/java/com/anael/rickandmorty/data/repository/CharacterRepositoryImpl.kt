package com.anael.rickandmorty.data.repository

import com.anael.rickandmorty.data.mapper.toDomain
import com.anael.rickandmorty.data.remote.EpisodesRemoteDataSource
import com.anael.rickandmorty.data.utils.safeCall
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.repository.CharacterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val remote: EpisodesRemoteDataSource,
) : CharacterRepository {

    override suspend fun getCharacter(id: String): Result<CharacterRnM> =
        safeCall { remote.getCharacterById(id) }
            .map { dto -> dto.toDomain() }

    override suspend fun getCharactersByIds(ids: List<String>): Result<List<CharacterRnM>> =
        safeCall {
            when (ids.size) {
                0 -> emptyList()
                1 -> listOf(remote.getCharacterById(ids.first()))
                else -> remote.getCharactersByIds(ids.joinToString(","))
            }
        }.map { list -> list.map { it.toDomain() } }
}
