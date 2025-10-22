package com.anael.rickandmorty.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anael.rickandmorty.presentation.activities.EpisodesActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class EpisodesScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<EpisodesActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun episodesList_showsExpectedItems() {
        // Example: check if the fake episodes are shown
        composeRule.onNodeWithText("Pilot").assertExists()
        composeRule.onNodeWithText("Lawnmower Dog").assertExists()
    }
}
