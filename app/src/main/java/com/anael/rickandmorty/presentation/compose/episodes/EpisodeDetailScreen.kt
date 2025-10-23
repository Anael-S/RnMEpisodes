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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anael.rickandmorty.R
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.presentation.compose.utils.ErrorState
import com.anael.rickandmorty.presentation.compose.utils.rememberErrorStrings
import com.anael.rickandmorty.presentation.ui.state.UiState
import com.anael.rickandmorty.presentation.viewmodel.EpisodeDetailsViewModel

object DetailsTestTags {
    const val LOADER = "details_loader"
    const val LIST = "details_list"
    const val ERROR = "details_error"
    const val APP_BAR_TITLE = "details_app_bar_title"
    const val HEADER_CODE   = "details_header_code"
}

@Composable
fun EpisodeDetailsScreen(
    onBackClick: () -> Unit,
    onCharacterClick: (String) -> Unit,
    viewModel: EpisodeDetailsViewModel = hiltViewModel(),
    episodeName: String,
    episodeCode: String
) {
    val state by viewModel.state.collectAsState()
    EpisodeDetailsScreen(
        onBackClick = onBackClick,
        onCharacterClick = onCharacterClick,
        state = state,
        onRetry = { viewModel.reload() },
        episodeName = episodeName,
        episodeCode = episodeCode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailsScreen(
    onBackClick: () -> Unit,
    onCharacterClick: (String) -> Unit,
    state: UiState<List<CharacterRnM>>,
    onRetry: () -> Unit,
    episodeName: String,
    episodeCode: String
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(episodeCode.ifBlank { stringResource(R.string.episode) }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                modifier = Modifier.testTag(DetailsTestTags.APP_BAR_TITLE)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (state) {
                UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.testTag(DetailsTestTags.LOADER))
                    }
                }
                is UiState.Error -> {
                    val (title, desc) = rememberErrorStrings(state.cause)
                    ErrorState(
                        title = title,
                        description = desc,
                        onRetry = onRetry,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag(DetailsTestTags.ERROR)
                    )
                }
                is UiState.Success -> {
                    DetailScreen(
                        episodeName = episodeName,
                        episodeCode = episodeCode,
                        characters = state.data,
                        onCharacterClick = onCharacterClick
                    )
                }
                UiState.Idle -> Unit
            }
        }
    }
}

/** Header with episode title + code, "Characters:" label, then list of characters. */
@Composable
private fun DetailScreen(
    episodeName: String,
    episodeCode: String,
    characters: List<CharacterRnM>,
    onCharacterClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(DetailsTestTags.LIST),
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
                    text = episodeCode,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(DetailsTestTags.HEADER_CODE),
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
                supportingContent = { Text(ch.id.toString()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCharacterClick(ch.id.toString()) }
            )
            HorizontalDivider()
        }
    }
}
