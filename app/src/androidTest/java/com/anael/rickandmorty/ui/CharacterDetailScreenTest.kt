package com.anael.rickandmorty.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anael.rickandmorty.R
import com.anael.rickandmorty.data.utils.NetworkError
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.model.Origin
import com.anael.rickandmorty.presentation.compose.characters.CharacterDetailScaffold
import com.anael.rickandmorty.presentation.compose.characters.CharacterTestTags
import com.anael.rickandmorty.presentation.ui.state.UiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleCharacter() = CharacterRnM(
        id = 42,
        name = "Rick Sanchez",
        species = "Human",
        status = "Alive",
        image = "https://rickandmortyapi.com/api/character/avatar/361.jpeg",
        origin = Origin(name = "Earth (C-137)", url = ""),
        episode = listOf("1", "2", "3")
    )

    private fun getString(resId: Int, vararg args: Any) =
        androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            .getString(resId, *args)

    @Test
    fun loading_showsProgress() {
        composeRule.setContent {
            CharacterDetailScaffold(
                state = UiState.Loading,
                onBackClick = {},
                onRetry = {},
                onExport = {}
            )
        }

        composeRule.onNodeWithTag(CharacterTestTags.LOADER).assertIsDisplayed()
        composeRule.onNodeWithTag(CharacterTestTags.TITLE).assertIsDisplayed()
    }

    @Test
    fun error_showsError_andRetryInvoked() {
        var retried = false
        composeRule.setContent {
            CharacterDetailScaffold(
                state = UiState.Error(NetworkError.Unknown(RuntimeException("Boom"))),
                onBackClick = {},
                onRetry = { retried = true },
                onExport = {}
            )
        }

        // Error container visible
        composeRule.onNodeWithTag(CharacterTestTags.ERROR).assertIsDisplayed()

        // Tap "Retry" (from your ErrorState composable)
        composeRule.onNodeWithText(getString(R.string.retry)).performClick()
        assert(retried)
    }

    @Test
    fun success_showsAllFields_exportAndBackWork() {
        val ch = sampleCharacter()
        var back = false
        var exported = false

        composeRule.setContent {
            CharacterDetailScaffold(
                state = UiState.Success(ch),
                onBackClick = { back = true },
                onRetry = {},
                onExport = { exported = true }
            )
        }

        // Title (unique via tag)
        composeRule.onNodeWithTag(CharacterTestTags.TITLE).assertIsDisplayed()
        // Body name: scope to the content area so we don't hit the title
        composeRule.onNode(
            hasText(ch.name) and hasAnyAncestor(hasTestTag(CharacterTestTags.CONTENT))
        ).assertIsDisplayed()


        // Image, content present
        composeRule.onNodeWithTag(CharacterTestTags.IMAGE).assertIsDisplayed()
        composeRule.onNodeWithTag(CharacterTestTags.CONTENT).assertIsDisplayed()

        // Status • Species
        composeRule.onNodeWithText("${ch.status} • ${ch.species}").assertIsDisplayed()

        // Origin
        composeRule.onNodeWithText(
            getString(R.string.origin_format, ch.origin.name)
        ).assertIsDisplayed()

        // Episodes count
        composeRule.onNodeWithText(
            getString(R.string.appears_in_n_episodes, ch.episode.size)
        ).assertIsDisplayed()

        // Export button visible & clickable
        composeRule.onNodeWithTag(CharacterTestTags.EXPORT_BTN).assertIsDisplayed().performClick()
        assert(exported)

        // Back button clickable (prefer contentDescription)
        composeRule.onNodeWithContentDescription(getString(R.string.back)).performClick()
        assert(back)
    }
}
