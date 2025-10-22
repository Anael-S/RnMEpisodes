package com.anael.rickandmorty.presentation.compose.characters

sealed interface CharacterUiEvent {
    data class RequestExport(val text: String, val suggestedFileName: String) : CharacterUiEvent
    data class ShowMessage(val messageRes: Int, val args: List<Any> = emptyList()) :
        CharacterUiEvent
}
