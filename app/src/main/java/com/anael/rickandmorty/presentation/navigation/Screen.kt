package com.anael.rickandmorty.presentation.navigation

object Graph {
    const val Episodes = "episodes_graph"
}

object Screen {
    const val Home = "home"
    const val EpisodeDetail = "episodeDetail"
    const val CharacterDetail = "character/{characterId}"
    fun character(characterId: String) = "character/$characterId"
}
