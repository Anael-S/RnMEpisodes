package com.anael.rickandmorty.domain.repository

import com.anael.rickandmorty.domain.model.CharacterRnM

/**
 * Interface for Characte Repository
 */
interface CharacterRepository {
    suspend fun getCharacter(id: String): Result<CharacterRnM>
    suspend fun getCharactersByIds(ids: List<String>): Result<List<CharacterRnM>>
}
