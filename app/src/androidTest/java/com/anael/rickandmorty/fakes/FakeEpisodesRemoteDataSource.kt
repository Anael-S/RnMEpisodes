package com.anael.rickandmorty.fakes

import com.anael.rickandmorty.data.model.CharacterDto
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PageInfoDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource

class FakeEpisodesRemoteDataSource(
    private val pages: Map<Int, PagedEpisodesDto>,
    private val episodesById: Map<String, EpisodeDto> = emptyMap(),
    private val charactersById: Map<String, CharacterDto> = emptyMap(),
) : RnMApiRemoteDataSource {

    override suspend fun getEpisodesPage(page: Int): PagedEpisodesDto {
        val fakePage1 = PagedEpisodesDto(
            info = PageInfoDto(
                count = 20,
                pages = 2,
                next = "https://rickandmortyapi.com/api/episode?page=2",
                prev = null
            ),
            results = listOf(
                EpisodeDto(
                    id = 1,
                    name = "Pilot",
                    airDate = "2017-12-02",
                    episode = "S01E01",
                    characters = emptyList(),
                    url = "",
                    created = ""
                ),
                EpisodeDto(
                    id = 2,
                    name = "Lawnmower Dog",
                    airDate = "2017-12-09",
                    episode = "S01E02",
                    characters = emptyList(),
                    url = "",
                    created = ""
                )
            )
        )

        val fakePage2 = PagedEpisodesDto(
            info = PageInfoDto(
                count = 20,
                pages = 2,
                next = null,
                prev = "https://rickandmortyapi.com/api/episode?page=1"
            ),
            results = listOf(
                EpisodeDto(
                    id = 3,
                    name = "Anatomy Park",
                    airDate = "2017-12-16",
                    episode = "S01E03",
                    characters = emptyList(),
                    url = "",
                    created = ""
                )
            )
        )

        val pages = mapOf(
            1 to fakePage1,
            2 to fakePage2
        )

        // Return the requested page, or a default empty one if out of range
        return pages[page] ?: PagedEpisodesDto(
            info = PageInfoDto(
                count = 0,
                pages = 0,
                next = null,
                prev = null
            ),
            results = emptyList()
        )
    }



    override suspend fun getEpisodeById(id: String): EpisodeDto =
        episodesById[id] ?: error("No episode $id")

    override suspend fun getCharacterById(id: String): CharacterDto =
        charactersById[id] ?: error("No character $id")

    override suspend fun getCharactersByIds(idsCsv: String): List<CharacterDto> =
        idsCsv.split(",").mapNotNull { charactersById[it] }
}
