package com.anael.rickandmorty.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    repository: EpisodesRepository
) : ViewModel() {
    val episodes = repository.getEpisodesStream().cachedIn(viewModelScope)
    val lastRefreshTime: StateFlow<Long?> =
        repository.lastRefreshFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

}
