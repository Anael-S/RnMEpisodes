package com.anael.rickandmorty.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anael.rickandmorty.presentation.compose.episodes.EpisodeDetailsScreen
import com.anael.rickandmorty.presentation.compose.episodes.EpisodesScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.anael.rickandmorty.presentation.viewmodel.EpisodeDetailsViewModel
import com.anael.rickandmorty.presentation.viewmodel.EpisodesSharedViewModel
import androidx.navigation.navigation
import com.anael.rickandmorty.presentation.compose.characters.CharacterDetailScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Graph.Episodes
    ) {
        navigation(
            route = Graph.Episodes,
            startDestination = Screen.Home
        ) {
            composable(Screen.Home) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Graph.Episodes)
                }
                val sharedVm: EpisodesSharedViewModel = hiltViewModel(parentEntry)

                EpisodesScreen(
                    onEpisodeClick = { ep ->
                        sharedVm.setSelected(
                            name = ep.name,
                            code = ep.episodeCode,
                            characterUrls = ep.characters
                        )
                        navController.navigate(Screen.EpisodeDetail)
                    }
                )
            }

            composable(Screen.EpisodeDetail) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Graph.Episodes)
                }
                val sharedVm: EpisodesSharedViewModel = hiltViewModel(parentEntry)

                val detailsVm: EpisodeDetailsViewModel = hiltViewModel(backStackEntry)

                val sel = sharedVm.selected.collectAsState().value
                LaunchedEffect(sel) {
                    sel?.let { detailsVm.loadWithIds(it.characterIds) }
                }

                EpisodeDetailsScreen(
                    onBackClick = { navController.navigateUp() },
                    onCharacterClick = { id -> navController.navigate(Screen.character(id)) },
                    viewModel = detailsVm,
                    episodeName = sel?.name.orEmpty(),
                    episodeCode = sel?.code.orEmpty()
                )
            }


            // --- CHARACTER DETAIL  ---
            composable(
                route = Screen.CharacterDetail,
                arguments = listOf(navArgument("characterId") { type = NavType.StringType })
            ) {
                CharacterDetailScreen(onBackClick = { navController.popBackStack() })
            }


        }
    }
}
