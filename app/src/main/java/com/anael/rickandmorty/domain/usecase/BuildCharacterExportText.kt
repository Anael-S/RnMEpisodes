package com.anael.rickandmorty.domain.usecase

import javax.inject.Inject
import com.anael.rickandmorty.domain.model.CharacterRnM

class BuildCharacterExportText @Inject constructor() {
    operator fun invoke(ch: CharacterRnM): String = buildString {
        appendLine("Name: ${ch.name}")
        appendLine("Status: ${ch.status}")
        appendLine("Species: ${ch.species}")
        appendLine("Origin: ${ch.origin.name}")
        appendLine("Total episodes: ${ch.episode.size}")
    }
}