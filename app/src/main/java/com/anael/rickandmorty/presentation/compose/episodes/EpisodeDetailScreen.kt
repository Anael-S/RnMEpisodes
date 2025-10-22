package com.anael.rickandmorty.presentation.compose.episodes


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anael.rickandmorty.R
import com.anael.rickandmorty.presentation.compose.utils.ErrorState
import com.anael.rickandmorty.presentation.compose.utils.rememberErrorStrings
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.presentation.ui.state.UiState
import com.anael.rickandmorty.presentation.viewmodel.EpisodeDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailsScreen(
    onBackClick: () -> Unit,
    onCharacterClick: (String) -> Unit,
    viewModel: EpisodeDetailsViewModel = hiltViewModel(),
    episodeName: String,
    episodeCode: String
) {
    // Characters loading state
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(episodeCode.ifBlank { stringResource(R.string.episode) }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val s = state) {
                UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    val (title, desc) = rememberErrorStrings(s.cause)
                    ErrorState(
                        title = title,
                        description = desc,
                        onRetry = { viewModel.reload() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Success -> {
                    DetailScreen(
                        episodeName = episodeName,
                        episodeCode = episodeCode,
                        characters = s.data,
                        onCharacterClick = onCharacterClick
                    )
                }

                UiState.Idle -> Unit
            }
        }
    }
}

/**
 * Header with episode title + code, "Characters:" label, then list of characters.
 */
@Composable
private fun DetailScreen(
    episodeName: String,
    episodeCode: String,
    characters: List<CharacterRnM>,
    onCharacterClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        item {
            Text(
                text = episodeName.ifBlank { "Episode" },
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(4.dp))
            if (episodeCode.isNotBlank()) {
                Text(
                    text = episodeCode, // e.g., "S01E01"
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = "Characters:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
        }

        // Characters list
        items(characters, key = { it.id }) { ch ->
            ListItem(
                headlineContent = { Text(ch.name) },
                supportingContent = { Text(ch.species) }, // "race" from API is `species`
                leadingContent = {
                    // If you use Coil:
                    // AsyncImage(model = ch.image, contentDescription = null, modifier = Modifier.size(40.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCharacterClick(ch.id.toString()) }
            )
            Divider()
        }
    }
}
