package dev.aladins.focustube.search

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.debug.HiltTestActivity
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.navigation.AppScaffold
import dev.aladins.focustube.navigation.AppUiState
import dev.aladins.focustube.testing.FakeYouTubeApi
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Classic search end to end with a fake YouTube API (0 real units). */
@HiltAndroidTest
class SearchFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var personas: PersonaRepository

    @Inject lateinit var api: FakeYouTubeApi

    @Inject lateinit var quota: QuotaTracker

    private fun text(@StringRes id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun waitFor(matcher: SemanticsMatcher) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        val persona =
            runBlocking {
                val id = personas.create("Guitar", "Learn fingerstyle guitar.", listOf("US", "GB", "DE").map(::Region))
                personas.setCurrent(id)
                personas.observe(id).first()!!
            }
        composeRule.setContent { FocusTubeTheme { AppScaffold(appState = AppUiState.Ready(persona)) } }
        composeRule.onNodeWithText(text(R.string.tab_search)).performClick()
        waitFor(hasSetTextAction())
    }

    private fun search(query: String) {
        composeRule.onNode(hasSetTextAction()).performTextInput(query)
        composeRule.onNode(hasSetTextAction()).performImeAction()
    }

    @Test
    fun resultsShowWithShortTags_andTheSameSearchAgainCallsYouTubeOnce() {
        search("fingerstyle basics")
        waitFor(hasText("Video fingerstyle-basics-US-p1-1"))
        assertEquals(
            true,
            composeRule.onAllNodes(
                hasText(text(R.string.tag_short).uppercase()),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        )
        assertEquals(listOf("US"), api.searchedRegions)

        composeRule.onNodeWithContentDescription(text(R.string.action_clear_search)).performClick()
        waitFor(hasText(text(R.string.search_recent).uppercase()))
        composeRule.onNodeWithText("fingerstyle basics").performClick()
        waitFor(hasText("Video fingerstyle-basics-US-p1-1"))
        assertEquals(1, api.searchCalls)
    }

    @Test
    fun tappingAResultOpensThePlayer() {
        search("fingerstyle basics")
        waitFor(hasText("Video fingerstyle-basics-US-p1-1"))
        composeRule.onNodeWithText("Video fingerstyle-basics-US-p1-1").performClick()
        waitFor(hasText(text(R.string.player_watching_as, "Guitar")))
        composeRule.onNodeWithText("Video fingerstyle-basics-US-p1-1").assertIsDisplayed()
    }

    @Test
    fun dailyLimitShowsTheBanner_keepsSavedResults_andDisablesRefresh() {
        search("fingerstyle basics")
        waitFor(hasText("Video fingerstyle-basics-US-p1-1"))
        quota.setTodayForDebugBlocking(QuotaTracker.DAILY_LIMIT)
        waitFor(hasText(text(R.string.limit_title)))
        composeRule.onNodeWithText("Video fingerstyle-basics-US-p1-1").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.action_refresh)).assertIsNotEnabled()
    }

    @Test
    fun dailyLimitOnAnUnsavedSearchSaysWhatToDo_andCallsNothing() {
        quota.setTodayForDebugBlocking(QuotaTracker.DAILY_LIMIT)
        search("travis picking")
        waitFor(hasText(text(R.string.search_limit_unsaved_message)))
        composeRule.onNodeWithText(text(R.string.limit_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.search_limit_unsaved_title)).assertIsDisplayed()
        assertEquals(0, api.searchCalls)
    }

    @Test
    fun regionSheetChangesThePersonasRegions() {
        composeRule.onNode(hasContentDescription(text(R.string.regions_chip_description, "US, UK, DE"))).performClick()
        waitFor(hasText(text(R.string.regions_title)))
        composeRule.onNodeWithText("United Kingdom").performClick()
        composeRule.onNodeWithText("Germany").performClick()
        composeRule.onNodeWithText(text(R.string.action_apply)).performClick()
        waitFor(hasContentDescription(text(R.string.regions_chip_description, "US")))
        runBlocking { assertEquals(listOf("US"), personas.observeCurrent().first()!!.regions.map { it.code }) }
    }
}
