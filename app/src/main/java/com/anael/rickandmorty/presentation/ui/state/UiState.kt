package com.anael.rickandmorty.presentation.ui.state

import com.anael.rickandmorty.data.utils.NetworkError

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val cause: NetworkError) : UiState<Nothing>()
}
