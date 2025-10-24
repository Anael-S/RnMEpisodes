package com.anael.rickandmorty.data.mapper

import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.OriginDto
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.model.Origin
import kotlinx.collections.immutable.toImmutableList

/**
 * Mapper class for DTO
 */
// DTO -> Domain
fun CharacterDto.toDomain() = CharacterRnM(
    id = id,
    name = name,
    status = status,
    species = species,
    origin = origin.toDomain(),
    image = image,
    episode = episode.toImmutableList()
)

private fun OriginDto.toDomain() = Origin(
    name = name,
    url = url
)
