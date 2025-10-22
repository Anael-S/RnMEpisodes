package com.anael.rickandmorty.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EpisodesSharedViewModel @Inject constructor() : ViewModel() {

    // what the detail screen needs (no network)
    data class SelectedEpisode(
        val name: String,
        val code: String,           // e.g. "S01E01"
        val characterIds: List<String>
    )

    private val _selected = MutableStateFlow<SelectedEpisode?>(null)
    val selected: StateFlow<SelectedEpisode?> = _selected

    fun setSelected(name: String, code: String, characterUrls: List<String>) {
        val ids = characterUrls
            .map { it.substringAfterLast('/') }
            .filter { it.all(Char::isDigit) }
        _selected.value = SelectedEpisode(name, code, ids)
    }
}
