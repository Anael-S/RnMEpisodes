package com.anael.rickandmorty.presentation.compose.characters

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.anael.rickandmorty.R
import com.anael.rickandmorty.presentation.compose.utils.ErrorState
import com.anael.rickandmorty.presentation.compose.utils.rememberErrorStrings
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.presentation.ui.state.UiState
import com.anael.rickandmorty.presentation.viewmodel.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    onBackClick: () -> Unit,
    viewModel: CharacterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // SAF launcher
    var pendingText by remember { mutableStateOf<String?>(null) }
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        val text = pendingText
        pendingText = null
        if (uri == null || text == null) return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.openOutputStream(uri, "w")?.use { os ->
                os.write(text.toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream")
        }.onSuccess {
            val name = (state as? UiState.Success)?.data?.name ?: ""
            viewModel.onExportSucceeded(name)
        }.onFailure {
            viewModel.onExportFailed()
        }
    }

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CharacterUiEvent.RequestExport -> {
                    pendingText = event.text
                    createDocLauncher.launch(event.suggestedFileName)
                }
                is CharacterUiEvent.ShowMessage -> {
                    val msg = if (event.args.isNotEmpty())
                        context.getString(event.messageRes, *event.args.toTypedArray())
                    else
                        context.getString(event.messageRes)
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { CharacterDetailTopBarTitle(state) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state is UiState.Success) {
                        IconButton(onClick = { viewModel.onExportClicked() }) {
                            Icon(Icons.Filled.Save, contentDescription = stringResource(R.string.export))
                        }
                    }
                }
            )
        }
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
                        onRetry = { viewModel.loadCharacter() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success<CharacterRnM> -> {
                    CharacterDetailContent(
                        character = s.data,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                UiState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun CharacterDetailTopBarTitle(state: UiState<CharacterRnM>) {
    val title = when (state) {
        is UiState.Success -> state.data.name
        else -> stringResource(R.string.character)
    }
    Text(title)
}

@Composable
private fun CharacterDetailContent(
    character: CharacterRnM,
    modifier: Modifier = Modifier
) {
    val episodesCount = character.episode.size

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = character.image,
            contentDescription = character.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.extraLarge),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = character.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${character.status} • ${character.species}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.origin_format, character.origin.name),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.appears_in_n_episodes, episodesCount),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))
    }
}
