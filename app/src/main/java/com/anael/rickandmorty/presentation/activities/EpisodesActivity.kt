
package com.anael.rickandmorty.presentation.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anael.rickandmorty.presentation.compose.EpisodesApp
import com.anael.rickandmorty.presentation.ui.RickAndMortyTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity/entry point, starting screen that display all episodes
 */
@AndroidEntryPoint
class EpisodesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            RickAndMortyTheme {
                EpisodesApp()
            }
        }
        
    }
}
