package com.anael.rickandmorty.presentation.navigation

/**
 * Helper to create route for our nav
 */
object Graph {
    const val Episodes = "episodes_graph"
}

object Screen {
    const val Home = "home"
    const val EpisodeDetail = "episodeDetail"
    const val CharacterDetail = "character/{characterId}"
    fun character(characterId: String) = "character/$characterId"
}
