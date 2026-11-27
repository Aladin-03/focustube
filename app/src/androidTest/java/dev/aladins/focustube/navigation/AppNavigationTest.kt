package dev.aladins.focustube.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.debug.HiltTestActivity
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import kotlin.time.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

/**
 * App shell navigation for a persona: the four tabs switch, the gear opens Settings, Back returns
 * to the persona's start tab. Runs with Hilt (Search and Settings have ViewModels) and a fake
 * YouTube API; persona screens are covered by FirstLaunchTest.
 */
@HiltAndroidTest
class AppNavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private fun text(@StringRes id: Int) = composeRule.activity.getString(id)

    private fun tab(@StringRes id: Int) = composeRule.onNodeWithText(text(id))

    /** Waits for pending clicks and navigation to settle first, then presses Back like the system does. */
    private fun pressSystemBack() {
        composeRule.waitForIdle()
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun start(persona: Persona = Guitar) {
        composeRule.setContent { FocusTubeTheme { AppScaffold(appState = AppUiState.Ready(persona)) } }
    }

    @Before
    fun setUp() {
        if (startsWithDefaultPersona) start()
    }

    /** Tests that need a different persona turn the default start off via this flag. */
    private val startsWithDefaultPersona: Boolean
        get() = !testName.methodName.startsWith("noGoal")

    @get:Rule
    val testName = TestName()

    @Test
    fun topBarShowsPersonaAndOpensPersonasFromName() {
        composeRule.onNodeWithText("Guitar").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.placeholder_picks_title)).assertIsDisplayed()
    }

    @Test
    fun noGoalPersonaStartsOnChannels_andBackFromPicksReturnsThere() {
        start(General)
        tab(R.string.tab_channels).assertIsSelected()
        tab(R.string.tab_picks).performClick()
        tab(R.string.tab_picks).assertIsSelected()
        pressSystemBack()
        tab(R.string.tab_channels).assertIsSelected()
    }

    @Test
    fun startsOnPicks() {
        tab(R.string.tab_picks).assertIsSelected()
        composeRule.onNodeWithText(text(R.string.placeholder_picks_title)).assertIsDisplayed()
    }

    @Test
    fun fourTabsSwitch() {
        listOf(
            R.string.tab_channels to R.string.channels_tab_new_uploads,
            R.string.tab_search to R.string.search_idle_message,
            R.string.tab_library to R.string.library_tab_history,
            R.string.tab_picks to R.string.placeholder_picks_title
        ).forEach { (tabLabel, title) ->
            tab(tabLabel).performClick()
            tab(tabLabel).assertIsSelected()
            composeRule.onNodeWithText(text(title)).assertIsDisplayed()
        }
        tab(R.string.tab_library).assertIsNotSelected()
    }

    @Test
    fun gearOpensSettingsWithoutTabs_andBackArrowReturns() {
        tab(R.string.tab_search).performClick()
        composeRule.onNodeWithContentDescription(text(R.string.action_settings)).performClick()

        composeRule.onNodeWithText(text(R.string.settings_default_regions)).assertIsDisplayed()
        tab(R.string.tab_picks).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        tab(R.string.tab_search).assertIsSelected()
        composeRule.onNodeWithText(text(R.string.search_idle_message)).assertIsDisplayed()
    }

    @Test
    fun systemBackFromSettingsReturnsToTab() {
        composeRule.onNodeWithContentDescription(text(R.string.action_settings)).performClick()
        composeRule.onNodeWithText(text(R.string.settings_default_regions)).assertIsDisplayed()

        pressSystemBack()
        tab(R.string.tab_picks).assertIsSelected()
    }

    @Test
    fun systemBackFromAnotherTabReturnsToPicks() {
        tab(R.string.tab_library).performClick()
        tab(R.string.tab_library).assertIsSelected()
        pressSystemBack()
        tab(R.string.tab_picks).assertIsSelected()
        composeRule.onNodeWithText(text(R.string.placeholder_picks_title)).assertIsDisplayed()
    }

    private companion object {
        val Guitar =
            Persona(
                1,
                "Guitar",
                "Learn fingerstyle guitar. I'm a beginner.",
                Region.Defaults,
                Instant.fromEpochMilliseconds(0)
            )
        val General = Persona(2, "General", null, Region.Defaults, Instant.fromEpochMilliseconds(0))
    }
}
