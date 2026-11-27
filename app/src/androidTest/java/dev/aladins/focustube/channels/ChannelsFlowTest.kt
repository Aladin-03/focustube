package dev.aladins.focustube.channels

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.debug.HiltTestActivity
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.ChannelRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.navigation.AppScaffold
import dev.aladins.focustube.navigation.AppUiState
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** My channels, Add channel and the channel page end to end (fake YouTube API, 0 real units). */
@HiltAndroidTest
class ChannelsFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var personas: PersonaRepository

    @Inject lateinit var channels: ChannelRepository

    private var general = 0L
    private var guitar = 0L

    private fun text(@StringRes id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun waitFor(matcher: SemanticsMatcher) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        val persona =
            runBlocking {
                guitar = personas.create("Guitar", "Learn fingerstyle guitar.", listOf(Region("US")))
                general = personas.create("General", null, listOf(Region("US")))
                personas.setCurrent(general)
                personas.observe(general).first()!!
            }
        // General has no goal: it opens on My channels.
        composeRule.setContent { FocusTubeTheme { AppScaffold(appState = AppUiState.Ready(persona)) } }
    }

    private fun addFredToBothPersonas() {
        waitFor(hasText(text(R.string.channels_empty_title)))
        composeRule.onNodeWithText(text(R.string.action_add_channel)).performClick()
        waitFor(hasSetTextAction())
        composeRule.onNode(hasSetTextAction()).performTextInput("@fred")
        composeRule.onNode(hasSetTextAction()).performImeAction()
        waitFor(hasContentDescription(text(R.string.add_channel_add_named, "Fred Channel")))
        composeRule.onNodeWithContentDescription(text(R.string.add_channel_add_named, "Fred Channel")).performClick()
        waitFor(hasText(text(R.string.add_channel_sheet_title, "Fred Channel")))
        composeRule.onNodeWithText("Guitar").performClick() // General is pre-ticked
        composeRule.onNodeWithText(text(R.string.action_save)).performClick()
        waitFor(hasText(text(R.string.add_channel_added).uppercase()))
        composeRule.onNodeWithContentDescription(text(R.string.action_back)).performClick()
    }

    @Test
    fun addingAChannelToTwoPersonasFillsTheFeed_andRemovingCanBeUndone() {
        addFredToBothPersonas()
        waitFor(hasText("Video fred-u0"))
        runBlocking { assertEquals(setOf(general, guitar), channels.observeFollowers("UC_fred").first()) }

        composeRule.onNodeWithText(text(R.string.channels_tab_channels, 1)).performClick()
        waitFor(hasText("Fred Channel"))
        composeRule.onNodeWithText("Fred Channel").performTouchInput { swipeLeft() }
        waitFor(hasText(text(R.string.channels_tab_channels, 0)))
        waitFor(hasText(text(R.string.action_undo)))
        composeRule.onNodeWithText(text(R.string.action_undo)).performClick()
        waitFor(hasText(text(R.string.channels_tab_channels, 1)))
    }

    @Test
    fun channelPageShowsWhoFollowsIt_sortsAndLoadsMore() {
        addFredToBothPersonas()
        composeRule.onNodeWithText(text(R.string.channels_tab_channels, 1)).performClick()
        waitFor(hasText("Fred Channel"))
        composeRule.onNodeWithText("Fred Channel").performClick()
        waitFor(hasText(text(R.string.channel_in, "Guitar, General")))
        composeRule.onNodeWithText(text(R.string.sort_popular)).performClick()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(text(R.string.action_more_uploads)))
        composeRule.onNodeWithText(text(R.string.action_more_uploads)).performClick()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Video fred-u11"))
        composeRule.onNodeWithText("Video fred-u11").assertIsDisplayed()
    }

    @Test
    fun playerChannelButtonOpensTheChannelPage() {
        addFredToBothPersonas()
        waitFor(hasText("Video fred-u0"))
        composeRule.onNodeWithText("Video fred-u0").performClick()
        waitFor(hasText(text(R.string.action_channel)))
        composeRule.onNodeWithText(text(R.string.action_channel)).performClick()
        // The fake's videos all name channel "channel-1": its page opens (fetched for 3 units).
        waitFor(hasText("Channel-1 Channel"))
    }
}
