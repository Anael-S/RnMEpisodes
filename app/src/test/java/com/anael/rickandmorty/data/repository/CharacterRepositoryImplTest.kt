package com.anael.rickandmorty.data.repository

import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.LocationDto
import com.anael.rickandmorty.data.model.OriginDto
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.HttpException

class CharacterRepositoryImplTest {

    private val remote: RnMApiRemoteDataSource = mockk()

    private val repo = CharacterRepositoryImpl(remote)

    // --- helpers ---
    private fun dtoRick() = CharacterDto(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        type = "",
        gender = "Male",
        origin = OriginDto("Earth (C-137)", "url"),
        location = LocationDto("Citadel of Ricks", "url"),
        image = "avatar",
        episode = listOf("ep1"),
        url = "char/1",
        created = "now"
    )

    @Test
    fun `getCharacter returns success result with mapped domain`() = runTest {
        coEvery { remote.getCharacterById("1") } returns dtoRick()

        val result = repo.getCharacter("1")

        assertThat(result.isSuccess).isTrue()
        val character: CharacterRnM = result.getOrThrow()
        assertThat(character.id).isEqualTo(1)
        assertThat(character.name).contains("Rick")
    }

    @Test
    fun `getCharacter returns failure when remote throws`() = runTest {
        coEvery { remote.getCharacterById("1") } throws httpException(500)

        val result = repo.getCharacter("1")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
    }

    @Test
    fun `getCharactersByIds returns empty list when ids empty`() = runTest {
        // remote not called
        val result = repo.getCharactersByIds(emptyList())

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `getCharactersByIds calls single endpoint when one id`() = runTest {
        coEvery { remote.getCharacterById("1") } returns dtoRick()

        val result = repo.getCharactersByIds(listOf("1"))
        val list = result.getOrThrow()

        assertThat(list).hasSize(1)
        assertThat(list.first().name).contains("Rick")
    }

    @Test
    fun `getCharactersByIds calls plural endpoint when many ids`() = runTest {
        val morty = dtoRick().copy(id = 2, name = "Morty Smith")
        coEvery { remote.getCharactersByIds("1,2") } returns listOf(dtoRick(), morty)

        val result = repo.getCharactersByIds(listOf("1", "2"))
        val list = result.getOrThrow()

        assertThat(list.map { it.name }).containsExactly("Rick Sanchez", "Morty Smith")
    }

    // --- small helper for HttpException ---
    private fun httpException(code: Int): HttpException {
        return HttpException(retrofit2.Response.error<Any>(code, okhttp3.ResponseBody.create(null, "")))
    }
}
