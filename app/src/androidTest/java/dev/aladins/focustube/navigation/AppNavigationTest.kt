package dev.aladins.focustube.navigation

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Milestone 1 acceptance: the four tabs switch, the gear opens Settings, Back returns. */
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(@StringRes id: Int) = composeRule.activity.getString(id)

    private fun tab(@StringRes id: Int) = composeRule.onNodeWithText(text(id))

    private fun pressSystemBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    @Before
    fun setUp() {
        composeRule.setContent { FocusTubeTheme { AppScaffold() } }
    }

    @Test
    fun startsOnPicks() {
        tab(R.string.tab_picks).assertIsSelected()
        composeRule.onNodeWithText(text(R.string.placeholder_picks_title)).assertIsDisplayed()
    }

    @Test
    fun fourTabsSwitch() {
        listOf(
            R.string.tab_channels to R.string.placeholder_channels_title,
            R.string.tab_search to R.string.placeholder_search_title,
            R.string.tab_library to R.string.placeholder_library_title,
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

        composeRule.onNodeWithText(text(R.string.placeholder_settings_title)).assertIsDisplayed()
        tab(R.string.tab_picks).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        tab(R.string.tab_search).assertIsSelected()
        composeRule.onNodeWithText(text(R.string.placeholder_search_title)).assertIsDisplayed()
    }

    @Test
    fun systemBackFromSettingsReturnsToTab() {
        composeRule.onNodeWithContentDescription(text(R.string.action_settings)).performClick()
        composeRule.onNodeWithText(text(R.string.placeholder_settings_title)).assertIsDisplayed()

        pressSystemBack()
        tab(R.string.tab_picks).assertIsSelected()
    }

    @Test
    fun systemBackFromAnotherTabReturnsToPicks() {
        tab(R.string.tab_library).performClick()
        pressSystemBack()
        tab(R.string.tab_picks).assertIsSelected()
        composeRule.onNodeWithText(text(R.string.placeholder_picks_title)).assertIsDisplayed()
    }
}
