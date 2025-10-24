package com.anael.rickandmorty.presentation.compose.characters

/**
 * Small helper to either export the character detail or show error message
 */
sealed interface CharacterUiEvent {
    data class RequestExport(val text: String, val suggestedFileName: String) : CharacterUiEvent
    data class ShowMessage(val messageRes: Int, val args: List<Any> = emptyList()) :
        CharacterUiEvent
}
