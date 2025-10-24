package com.anael.rickandmorty.presentation.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.anael.rickandmorty.presentation.navigation.AppNavHost

/**
 * App to start the nav
 */
@Composable
fun EpisodesApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface {
            AppNavHost(navController = navController)
        }
    }
}
