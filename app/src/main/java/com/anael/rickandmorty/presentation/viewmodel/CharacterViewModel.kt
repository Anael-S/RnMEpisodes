package com.anael.rickandmorty.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anael.rickandmorty.R
import com.anael.rickandmorty.presentation.compose.characters.CharacterUiEvent
import com.anael.rickandmorty.domain.repository.CharacterRepository
import com.anael.rickandmorty.domain.model.CharacterRnM                 // CHANGE
import com.anael.rickandmorty.domain.usecase.BuildCharacterExportText
import com.anael.rickandmorty.data.utils.toNetworkError
import com.anael.rickandmorty.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val buildCharacterExportText: BuildCharacterExportText,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: String = checkNotNull(savedStateHandle["characterId"])

    private val _state = MutableStateFlow<UiState<CharacterRnM>>(UiState.Idle)   // CHANGE
    val state: StateFlow<UiState<CharacterRnM>> = _state.asStateFlow()           // CHANGE

    init {
        loadCharacter()
    }

    fun loadCharacter() = viewModelScope.launch {
        _state.value = UiState.Loading
        val result = repository.getCharacter(characterId) // Result<Character>
        _state.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.toNetworkError()) }
        )
    }

    private val _events = MutableSharedFlow<CharacterUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CharacterUiEvent> = _events

    fun onExportClicked() {
        val current = (state.value as? UiState.Success)?.data ?: return
        val text = buildCharacterExportText(current) // ensure usecase expects domain Character

        val safeName = current.name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

        _events.tryEmit(
            CharacterUiEvent.RequestExport(
                text = text,
                suggestedFileName = "${safeName}_character.txt"
            )
        )
    }

    fun onExportSucceeded(characterName: String) {
        _events.tryEmit(
            CharacterUiEvent.ShowMessage(
                messageRes = R.string.export_success_snackbar,
                args = listOf(characterName)
            )
        )
    }

    fun onExportFailed() {
        _events.tryEmit(
            CharacterUiEvent.ShowMessage(
                messageRes = R.string.export_failure_snackbar
            )
        )
    }
}
