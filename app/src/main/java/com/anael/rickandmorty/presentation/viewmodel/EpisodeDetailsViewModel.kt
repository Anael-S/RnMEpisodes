package com.anael.rickandmorty.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anael.rickandmorty.domain.model.CharacterRnM              // CHANGE
import com.anael.rickandmorty.domain.repository.CharacterRepository
import com.anael.rickandmorty.data.utils.toNetworkError
import com.anael.rickandmorty.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel @Inject constructor(
    private val repo: CharacterRepository
) : ViewModel() {

    private var lastIds: List<String> = emptyList()

    private val _state = MutableStateFlow<UiState<List<CharacterRnM>>>(UiState.Idle)
    val state: StateFlow<UiState<List<CharacterRnM>>> = _state

    fun loadWithIds(ids: List<String>) = viewModelScope.launch {
        lastIds = ids
        _state.value = UiState.Loading
        val result = repo.getCharactersByIds(ids) // Result<List<Character>>
        _state.value = result.fold(
            onSuccess = { UiState.Success(it.sortedBy { c -> c.name }) },
            onFailure = { UiState.Error(it.toNetworkError()) }
        )
    }

    fun reload() {
        if (lastIds.isNotEmpty()) {
            viewModelScope.launch { loadWithIds(lastIds) }
        } else {
            _state.value = UiState.Idle
        }
    }
}
