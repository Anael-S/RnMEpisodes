package com.anael.rickandmorty.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anael.rickandmorty.R
import com.anael.rickandmorty.data.utils.NetworkError
import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.model.Origin
import com.anael.rickandmorty.presentation.compose.episodes.DetailsTestTags
import com.anael.rickandmorty.presentation.compose.episodes.EpisodeDetailsScreen
import com.anael.rickandmorty.presentation.ui.state.UiState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpisodeDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleCharacters() = listOf(
        CharacterRnM(
            id = 1, name = "Rick Sanchez", species = "Human", image = "",
            status = "", origin = Origin(name = "Earth", url = ""), episode = persistentListOf("1", "2")
        ),
        CharacterRnM(
            id = 2, name = "Morty Smith", species = "Human", image = "",
            status = "", origin = Origin(name = "Earth", url = ""), episode = persistentListOf("1", "2")
        ),
    )

    private fun getString(resId: Int) =
        androidx.test.core.app.ApplicationProvider
            .getApplicationContext<android.content.Context>()
            .getString(resId)

    @Test
    fun loading_showsCircularProgress() {
        composeRule.setContent {
            EpisodeDetailsScreen(
                onBackClick = {},
                onCharacterClick = {},
                state = UiState.Loading,
                onRetry = {},
                episodeName = "Pilot",
                episodeCode = "S01E01"
            )
        }

        // App bar shows code
        composeRule.onAllNodesWithText("S01E01").onFirst().assertIsDisplayed()
        // Loader is tagged
        composeRule.onNodeWithTag(DetailsTestTags.LOADER).assertIsDisplayed()
    }

    @Test
    fun error_showsErrorState_andRetryCallsCallback() {
        var retried = false
        composeRule.setContent {
            EpisodeDetailsScreen(
                onBackClick = {},
                onCharacterClick = {},
                state = UiState.Error(NetworkError.Unknown(RuntimeException("Boom"))),
                onRetry = { retried = true },
                episodeName = "Pilot",
                episodeCode = "S01E01"
            )
        }

        // Error container renders and Retry works
        composeRule.onNodeWithTag(DetailsTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.retry))
            .assertIsDisplayed()
            .performClick()

        assert(retried)
    }

    @Test
    fun success_showsHeader_andCharacters_andClickWorks() {
        var clickedId: String? = null
        val chars = sampleCharacters()

        composeRule.setContent {
            EpisodeDetailsScreen(
                onBackClick = {},
                onCharacterClick = { clickedId = it },
                state = UiState.Success(chars),
                onRetry = {},
                episodeName = "Pilot",
                episodeCode = "S01E01"
            )
        }

        // Header texts
        composeRule.onNodeWithText("Pilot").assertIsDisplayed()


        composeRule.onNodeWithTag(DetailsTestTags.APP_BAR_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithText("S01E01").onFirst()
            .assertIsDisplayed() //onFirst because there's the top bar + the title
        composeRule.onNodeWithTag(DetailsTestTags.HEADER_CODE).assertIsDisplayed()
        composeRule.onAllNodesWithText("S01E01").assertCountEquals(2) //topBar + the title


        composeRule.onNodeWithText("Characters:").assertIsDisplayed()

        // List items
        composeRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        composeRule.onNodeWithText("Morty Smith").assertIsDisplayed()

        // Click a character
        composeRule.onNodeWithText("Morty Smith").performClick()
        assert(clickedId == "2")
    }

    @Test
    fun backButton_invokesCallback() {
        var back = false
        composeRule.setContent {
            EpisodeDetailsScreen(
                onBackClick = { back = true },
                onCharacterClick = {},
                state = UiState.Success(sampleCharacters()),
                onRetry = {},
                episodeName = "Pilot",
                episodeCode = "S01E01"
            )
        }

        // Click by content description we set on the icon
        composeRule.onNodeWithContentDescription(getString(R.string.back)).performClick()
        assert(back)
    }
}
