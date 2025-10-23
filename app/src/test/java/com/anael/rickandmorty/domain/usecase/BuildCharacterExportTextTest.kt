package com.anael.rickandmorty.domain.usecase

import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.model.Origin
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class BuildCharacterExportTextTest {

    private val usecase = BuildCharacterExportText()

    @Test
    fun `builds export text with all fields`() {
        val character = CharacterRnM(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            origin = Origin(
                name = "Earth (C-137)",
                url = "https://rickandmortyapi.com/api/location/1"
            ),
            episode = persistentListOf("S01E01", "S01E02", "S01E03"),
            image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
        )

        val actual = usecase(character)

        val expected = buildString {
            appendLine("Name: Rick Sanchez")
            appendLine("Status: Alive")
            appendLine("Species: Human")
            appendLine("Origin: Earth (C-137)")
            appendLine("Total episodes: 3")
        }

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `handles empty episodes and blank origin name`() {
        val character = CharacterRnM(
            id = 2,
            name = "Morty Smith",
            status = "Unknown",
            species = "Human",
            origin = Origin(name = "", url = ""),
            image = "",
            episode = persistentListOf(),
        )

        val actual = usecase(character)

        val expected = buildString {
            appendLine("Name: Morty Smith")
            appendLine("Status: Unknown")
            appendLine("Species: Human")
            appendLine("Origin: ")
            appendLine("Total episodes: 0")
        }

        assertThat(actual).isEqualTo(expected)
    }
}
