package com.anael.rickandmorty.data.mapper

import com.anael.rickandmorty.data.local.EpisodeEntity
import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.LocationDto
import com.anael.rickandmorty.data.model.OriginDto
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.model.Episode
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    // -------- CharacterDto.toDomain() --------
    @Test
    fun `character dto maps to domain correctly`() {
        val originDto = OriginDto(name = "Earth", url = "https://earth")
        val dto = CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            origin = originDto,
            image = "image_url",
            episode = listOf("ep1", "ep2"),
            created = "",
            gender = "",
            location = LocationDto(name = "", url = ""),
            type = "",
            url = "",
        )

        val domain: CharacterRnM = dto.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Rick Sanchez", domain.name)
        assertEquals("Alive", domain.status)
        assertEquals("Human", domain.species)
        assertEquals("Earth", domain.origin.name)
        assertEquals("https://earth", domain.origin.url)
        assertEquals("image_url", domain.image)
        assertEquals(listOf("ep1", "ep2"), domain.episode)
    }

    // -------- EpisodeDto.toEntity() --------
    @Test
    fun `episode dto maps to entity correctly`() {
        val dto = EpisodeDto(
            id = 10,
            name = "Pilot",
            airDate = "December 2, 2013",
            episode = "S01E01",
            characters = listOf("Rick", "Morty"),
            url = "https://episode-url",
            created = "time"
        )

        val entity: EpisodeEntity = dto.toEntity()

        assertEquals(10, entity.id)
        assertEquals("Pilot", entity.name)
        assertEquals("December 2, 2013", entity.airDate)
        assertEquals("S01E01", entity.episodeCode)
        assertEquals(listOf("Rick", "Morty"), entity.characters)
        assertEquals("https://episode-url", entity.url)
        assertEquals("time", entity.created)
    }

    // -------- EpisodeEntity.toDto() --------
    @Test
    fun `episode entity maps to dto correctly`() {
        val entity = EpisodeEntity(
            id = 10,
            name = "Pilot",
            airDate = "December 2, 2013",
            episodeCode = "S01E01",
            characters = listOf("Rick", "Morty"),
            url = "https://episode-url",
            created = "time"
        )

        val dto: EpisodeDto = entity.toDto()

        assertEquals(10, dto.id)
        assertEquals("Pilot", dto.name)
        assertEquals("December 2, 2013", dto.airDate)
        assertEquals("S01E01", dto.episode)
        assertEquals(listOf("Rick", "Morty"), dto.characters)
        assertEquals("https://episode-url", dto.url)
        assertEquals("time", dto.created)
    }

    // -------- EpisodeDto.toDomain() --------
    @Test
    fun `episode dto maps to domain correctly`() {
        val dto = EpisodeDto(
            id = 5,
            name = "Lawnmower Dog",
            airDate = "December 9, 2013",
            episode = "S01E02",
            characters = listOf("Rick", "Morty"),
            url = "https://episode-url",
            created = "time"
        )

        val domain: Episode = dto.toDomain()

        assertEquals(5, domain.id)
        assertEquals("Lawnmower Dog", domain.name)
        assertEquals("December 9, 2013", domain.airDate)
        assertEquals("S01E02", domain.episodeCode)
        assertEquals(listOf("Rick", "Morty"), domain.characters)
    }

    // -------- EpisodeEntity.toDomain() --------
    @Test
    fun `episode entity maps to domain correctly`() {
        val entity = EpisodeEntity(
            id = 5,
            name = "Lawnmower Dog",
            airDate = "December 9, 2013",
            episodeCode = "S01E02",
            characters = listOf("Rick", "Morty"),
            url = "https://episode-url",
            created = "time"
        )

        val domain: Episode = entity.toDomain()

        assertEquals(5, domain.id)
        assertEquals("Lawnmower Dog", domain.name)
        assertEquals("December 9, 2013", domain.airDate)
        assertEquals("S01E02", domain.episodeCode)
        assertEquals(listOf("Rick", "Morty"), domain.characters)
    }
}
