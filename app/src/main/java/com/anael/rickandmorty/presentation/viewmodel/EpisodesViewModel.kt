package com.anael.rickandmorty.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.anael.rickandmorty.data.repository.EpisodesRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    repository: EpisodesRepositoryImpl
) : ViewModel() {
    val episodes = repository.getEpisodesStream()
        .cachedIn(viewModelScope)
    val lastRefreshTime: StateFlow<Long?> =
        repository.lastRefreshFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

}
