package dev.aladins.focustube.library

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.debug.HiltTestActivity
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.repository.HistoryRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.WatchLaterRepository
import dev.aladins.focustube.navigation.AppScaffold
import dev.aladins.focustube.navigation.AppUiState
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Watch later and History end to end (fake YouTube API, 0 real units). */
@HiltAndroidTest
class LibraryFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var personas: PersonaRepository

    @Inject lateinit var watchLater: WatchLaterRepository

    @Inject lateinit var history: HistoryRepository

    private var personaId = 0L

    private fun text(@StringRes id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun waitFor(matcher: SemanticsMatcher) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        val persona =
            runBlocking {
                personaId = personas.create("Guitar", "Learn fingerstyle guitar.", listOf(Region("US")))
                personas.setCurrent(personaId)
                personas.observe(personaId).first()!!
            }
        composeRule.setContent { FocusTubeTheme { AppScaffold(appState = AppUiState.Ready(persona)) } }
    }

    private fun searchAndSaveFirstTwo() {
        composeRule.onNodeWithText(text(R.string.tab_search)).performClick()
        waitFor(hasSetTextAction())
        composeRule.onNode(hasSetTextAction()).performTextInput("fingerstyle basics")
        composeRule.onNode(hasSetTextAction()).performImeAction()
        waitFor(hasText("Video fingerstyle-basics-US-p1-0"))
        val save = text(R.string.action_save_watch_later)
        composeRule.onAllNodesWithContentDescription(save)[0].performClick()
        waitFor(hasContentDescription(text(R.string.action_remove_watch_later)))
        composeRule.onAllNodesWithContentDescription(save)[0].performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(
                hasContentDescription(text(R.string.action_remove_watch_later))
            ).fetchSemanticsNodes().size ==
                2
        }
    }

    @Test
    fun savingFromSearchShowsInLibrary_andRemoveThenUndoBringsItBack() {
        searchAndSaveFirstTwo()
        composeRule.onNodeWithText(text(R.string.tab_library)).performClick()
        waitFor(hasText(text(R.string.library_tab_watch_later, 2)))
        composeRule.onNodeWithText("Video fingerstyle-basics-US-p1-0").assertIsDisplayed()

        // Swipe left to remove, then Undo.
        composeRule.onNodeWithText("Video fingerstyle-basics-US-p1-0").performTouchInput { swipeLeft() }
        waitFor(hasText(text(R.string.library_tab_watch_later, 1)))
        waitFor(hasText(text(R.string.action_undo)))
        composeRule.onNodeWithText(text(R.string.action_undo)).performClick()
        waitFor(hasText(text(R.string.library_tab_watch_later, 2)))
        runBlocking { assertEquals(2, watchLater.observeIds(personaId).first().size) }
    }

    @Test
    fun historyShowsWatchedAndStoppedAt_andClearAsksFirst() {
        searchAndSaveFirstTwo()
        runBlocking {
            history.save(personaId, "fingerstyle-basics-US-p1-1", WatchProgress(400, 664, true), Clock.System.now())
            history.save(personaId, "fingerstyle-basics-US-p1-2", WatchProgress(410, 664, false), Clock.System.now())
        }
        composeRule.onNodeWithText(text(R.string.tab_library)).performClick()
        waitFor(hasText(text(R.string.library_tab_history)))
        composeRule.onNodeWithText(text(R.string.library_tab_history)).performClick()
        waitFor(hasText(text(R.string.day_today)))
        composeRule.onNodeWithText(text(R.string.tag_watched).uppercase(), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(text(R.string.library_stopped_at, "6:50"), useUnmergedTree = true).assertExists()

        composeRule.onNodeWithText(text(R.string.action_clear_history)).performClick()
        waitFor(hasText(text(R.string.clear_history_title)))
        composeRule.onNodeWithText(text(R.string.action_clear)).performClick()
        waitFor(hasText(text(R.string.library_history_empty_title)))
        runBlocking {
            assertTrue(history.observe(personaId).first().isEmpty())
            assertEquals(2, watchLater.observeIds(personaId).first().size) // Watch later untouched
        }
    }

    @Test
    fun playerToggleSavesAndUnsaves() {
        searchAndSaveFirstTwo()
        composeRule.onNodeWithText("Video fingerstyle-basics-US-p1-3").performClick()
        waitFor(hasText(text(R.string.player_watching_as, "Guitar")))
        composeRule.onNodeWithText(text(R.string.watch_later)).performClick()
        composeRule.waitUntil(5_000) {
            runBlocking {
                "fingerstyle-basics-US-p1-3" in
                    watchLater.observeIds(personaId).first()
            }
        }
        composeRule.onNodeWithText(text(R.string.watch_later)).performClick()
        composeRule.waitUntil(5_000) {
            runBlocking {
                "fingerstyle-basics-US-p1-3" !in
                    watchLater.observeIds(personaId).first()
            }
        }
    }
}
