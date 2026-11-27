package dev.aladins.focustube.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.feature.player.PlayerActions
import dev.aladins.focustube.feature.player.PlayerScreen
import dev.aladins.focustube.feature.player.PlayerUiState
import kotlin.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** FocusTube's end state covers the player when a video ends; Replay hides it again. */
class PlayerEndStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val video =
        Video(
            "dQw4w9WgXcQ", "Fingerstyle basics", "c1", "Fingerstyle Fred", 664,
            Instant.parse(
                "2026-11-01T00:00:00Z"
            ),
            84_000, false, "About", null
        )

    @Test
    fun endedShowsNothingPlaysNext_andReplayClearsIt() {
        var replayed = false
        composeRule.setContent {
            FocusTubeTheme {
                var state by remember {
                    mutableStateOf(PlayerUiState(video.id, video, "Guitar", isLoading = false, hasEnded = true))
                }
                PlayerScreen(
                    state = state,
                    actions =
                        PlayerActions(
                            onBack = {},
                            onProgress = {},
                            onPaused = {},
                            onEnded = {},
                            onReplay = {
                                replayed = true
                                state = state.copy(hasEnded = false)
                            },
                            onUnplayable = {},
                            onToggleWatchLater = {},
                            onOpenChannel = {}
                        )
                )
            }
        }
        val end = context.getString(R.string.player_end)
        assertTrue(composeRule.onAllNodesWithText(end).fetchSemanticsNodes().size >= 2) // overlay + end mark
        composeRule.onNodeWithText(context.getString(R.string.action_replay)).assertIsDisplayed().performClick()
        assertTrue(replayed)
        composeRule.onNodeWithText(context.getString(R.string.action_replay)).assertDoesNotExist()
    }
}
