package com.anael.rickandmorty.data.mapper

import com.anael.rickandmorty.data.local.EpisodeEntity
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.domain.model.Episode
import kotlinx.collections.immutable.toImmutableList

/**
 * Mapper class for DTO / Entity / Domain
 */
// DTO -> Entity
fun EpisodeDto.toEntity() = EpisodeEntity(
    id = id,
    name = name,
    airDate = airDate,
    episodeCode = episode,
    characters = characters,
    url = url,
    created = created
)

// Entity -> DTO
fun EpisodeEntity.toDto() = EpisodeDto(
    id = id,
    name = name,
    airDate = airDate,
    episode = episodeCode,
    characters = characters,
    url = url,
    created = created
)

// DTO -> Domain
fun EpisodeDto.toDomain() = Episode(
    id = id,
    name = name,
    airDate = airDate,
    episodeCode = episode,
    characters = characters.toImmutableList()
)

// Entity -> Domain
fun EpisodeEntity.toDomain() = Episode(
    id = id,
    name = name,
    airDate = airDate,
    episodeCode = episodeCode,
    characters = characters.toImmutableList()
)
