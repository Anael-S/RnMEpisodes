package com.anael.rickandmorty.domain.repository

import com.anael.rickandmorty.domain.model.CharacterRnM

interface CharacterRepository {
    suspend fun getCharacter(id: String): Result<CharacterRnM>
    suspend fun getCharactersByIds(ids: List<String>): Result<List<CharacterRnM>>
}
