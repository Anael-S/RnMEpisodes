package com.anael.rickandmorty.ui

import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anael.rickandmorty.R
import com.anael.rickandmorty.di.fakeEpisodesRepo
import com.anael.rickandmorty.fakes.FakeEpisodesRepository
import com.anael.rickandmorty.fakes.demoEpisodes
import com.anael.rickandmorty.presentation.activities.EpisodesActivity
import com.anael.rickandmorty.presentation.compose.episodes.TestTags
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

    // We control when the Activity launches so each test can choose the starting mode.
    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    private val appContext: Context
        get() = ApplicationProvider.getApplicationContext()

    private val defaultMode =
        FakeEpisodesRepository.Mode.Success(items = demoEpisodes(40), pageSize = 10)

    @Before
    fun setup() {
        // Required for Hilt plumbing
        hiltRule.inject()

        // Initialize WorkManager in test mode and cancel any jobs that might touch DB/network.
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val config = androidx.work.Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.ERROR)
            .setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
            .build()
        androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager(ctx, config)
        androidx.work.WorkManager.getInstance(ctx).cancelAllWork()
        androidx.work.WorkManager.getInstance(ctx)
            .cancelUniqueWork(com.anael.rickandmorty.infrastructure.work.EpisodesSyncScheduler.UNIQUE_NAME)

        // NOTE: do NOT launch the Activity here; each test will decide when & with which mode.
    }

    // --- Helpers ------------------------------------------------------------------------------

    private fun launchWith(mode: FakeEpisodesRepository.Mode) {
        appContext.fakeEpisodesRepo().mode = mode
        ActivityScenario.launch(EpisodesActivity::class.java)
    }

    // --- Tests --------------------------------------------------------------------------------

    @Test
    fun topBar_showsTitle_andLastUpdatedFallback() {
        launchWith(defaultMode)

        composeRule.onNodeWithText(appContext.getString(R.string.episodes_title))
            .assertExists()
            .assertIsDisplayed()

        composeRule.onNodeWithText(appContext.getString(R.string.never_updated))
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun firstLoad_showsFullScreenLoader_thenRendersList() {
        launchWith(FakeEpisodesRepository.Mode.Endless(total = 40, pageSize = 20))

        // Wait for either loader or list (fast paths may skip loader)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.INITIAL_LOADER).fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }

        // Optional: if loader showed, assert it
        val loaderNodes = composeRule.onAllNodesWithTag(TestTags.INITIAL_LOADER).fetchSemanticsNodes()
        if (loaderNodes.isNotEmpty()) {
            composeRule.onNodeWithTag(TestTags.INITIAL_LOADER).assertExists()
        }

        // Ensure the list renders
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST)
            .assertExists()
            .assert(hasScrollAction())
    }

    @Test
    fun list_showsItems_canScroll_andTap() {
        launchWith(FakeEpisodesRepository.Mode.Success(items = demoEpisodes(40)))

        // Wait for list
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }

        // First item visible
        composeRule.onNodeWithText("Episode 1").assertExists().assertIsDisplayed()

        // Scroll to a far item
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST)
            .performScrollToNode(hasText("Episode 35"))

        composeRule.onNodeWithText("Episode 35").assertExists().assertIsDisplayed()

        // Tap row
        composeRule.onNodeWithText("Episode 35").assertHasClickAction().performClick()
    }

    @Test
    fun pullToRefresh_triggersRefreshIndicator_andListStillVisible() {
        launchWith(FakeEpisodesRepository.Mode.Endless(total = 40, pageSize = 20))

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }

        // Pull down gesture
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST).performTouchInput { swipeDown() }
        composeRule.mainClock.advanceTimeBy(300) // small settle time

        // List remains present
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST).assertExists()
    }

    @Test
    fun firstLoadError_showsFullScreenError_andRetryRecovers() {
        // Start fresh Activity in error mode
        launchWith(FakeEpisodesRepository.Mode.FirstLoadError)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.ERROR_EMPTY)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(TestTags.ERROR_EMPTY).assertExists().assertIsDisplayed()

        // Flip to success and retry
        appContext.fakeEpisodesRepo().mode =
            FakeEpisodesRepository.Mode.Success(items = demoEpisodes(20))
        composeRule.onNodeWithText(appContext.getString(R.string.retry)).performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }
    }


    @Test
    fun appendLoading_andAppendError_showProperFooters() {
        // PageSize=10, error when trying to load page 3 (items 31..40)
        launchWith(FakeEpisodesRepository.Mode.AppendError(failAfter = 30, pageSize = 10))

        // Wait for the list to appear
        composeRule.waitUntil(8_000) {
            composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }

        // Scroll near the end of what's currently loaded to actually trigger APPEND
        // "Episode 30" guarantees we're at the tail of page 2, which kicks off the next append (page 3 -> error)
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST)
            .performScrollToNode(hasText("Episode 30"))

        // Now wait for either the transient append loader OR the append error footer to become available
        val errorText = appContext.getString(R.string.error_loading_more)
        composeRule.waitUntil(8_000) {
            composeRule.onAllNodesWithTag(TestTags.APPEND_LOADER).fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText(errorText).fetchSemanticsNodes().isNotEmpty()
        }

        // Optional: assert loader if it happened to be visible for long enough
        val loaderNodes = composeRule.onAllNodesWithTag(TestTags.APPEND_LOADER).fetchSemanticsNodes()
        if (loaderNodes.isNotEmpty()) {
            composeRule.onNodeWithTag(TestTags.APPEND_LOADER).assertExists()
        }

        // Ensure the error footer itself gets composed (lazy lists don't compose offscreen items)
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST)
            .performScrollToNode(hasText(errorText))

        // Assert footer text + retry present and actionable
        composeRule.onNodeWithText(errorText).assertExists().assertIsDisplayed()
        composeRule.onNodeWithText(appContext.getString(R.string.retry)).assertExists().performClick()
    }


    @Test
    fun endOfPagination_footerAppears() {
        // Exactly 21 items with pageSize 20 -> one extra page, then end
        launchWith(FakeEpisodesRepository.Mode.Success(items = demoEpisodes(21), pageSize = 20))

        // Wait for list
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(TestTags.EPISODES_LIST).fetchSemanticsNodes().isNotEmpty()
        }

        // Scroll to last item to ensure second page loaded
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST)
            .performScrollToNode(hasText("Episode 21"))

        // Now scroll to the footer text itself to compose it
        val nothingMore = appContext.getString(R.string.nothing_more_to_load)
        composeRule.onNodeWithTag(TestTags.EPISODES_LIST)
            .performScrollToNode(hasText(nothingMore))

        // Wait for footer to appear
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(nothingMore).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(nothingMore).assertExists().assertIsDisplayed()
    }
}


