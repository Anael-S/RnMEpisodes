package com.anael.rickandmorty.presentation.compose

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

object Graph {
    const val Episodes = "episodes_graph"
}

object Screen {
    const val Home = "home"
    const val EpisodeDetail = "episodeDetail"
    const val CharacterDetail = "character/{characterId}"
    fun character(characterId: String) = "character/$characterId"
}
