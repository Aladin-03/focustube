package dev.aladins.focustube.onboarding

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aladins.focustube.MainActivity
import dev.aladins.focustube.R
import org.junit.Rule
import org.junit.Test

/**
 * The whole app with an empty in-memory database: Welcome first, then the persona's start tab,
 * then a second persona without a goal, switching and archiving.
 */
@HiltAndroidTest
class FirstLaunchTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(@StringRes id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun nameField() = composeRule.onAllNodes(hasSetTextAction())[0]

    private fun goalField() = composeRule.onAllNodes(hasSetTextAction())[1]

    private fun waitForText(value: String) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(hasText(value)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitForDescription(value: String) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasContentDescription(value)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun firstLaunchShowsWelcome_createWithGoalOpensPicks() {
        waitForText(text(R.string.welcome_title))
        composeRule.onNodeWithText(text(R.string.action_create_persona)).assertIsNotEnabled()

        nameField().performTextInput("Guitar")
        goalField().performTextInput("Learn fingerstyle guitar. I'm a beginner.")
        composeRule.onNodeWithText(
            text(R.string.action_create_persona)
        ).performScrollTo().assertIsEnabled().performClick()

        waitForText(text(R.string.placeholder_picks_title))
        composeRule.onNodeWithText(text(R.string.tab_picks)).assertIsSelected()
        composeRule.onNodeWithText("Guitar").assertIsDisplayed()
    }

    @Test
    fun tooLongNameShowsErrorAndBlocksCreate() {
        waitForText(text(R.string.welcome_title))
        nameField().performTextInput("a".repeat(31))
        composeRule.onNodeWithText(
            composeRule.activity.resources.getQuantityString(R.plurals.persona_error_too_long, 30, 30)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_create_persona)).assertIsNotEnabled()
    }

    @Test
    fun secondPersonaWithoutGoalOpensChannels_switchBackAndArchive() {
        waitForText(text(R.string.welcome_title))
        nameField().performTextInput("Guitar")
        goalField().performTextInput("Learn fingerstyle guitar.")
        composeRule.onNodeWithText(text(R.string.action_create_persona)).performScrollTo().performClick()
        waitForText("Guitar")

        // New persona without a goal: switches to it and opens My channels.
        composeRule.onNodeWithText("Guitar").performClick()
        waitForText(text(R.string.personas_new))
        composeRule.onNodeWithText(text(R.string.personas_new)).performClick()
        waitForText(text(R.string.edit_persona_title_new))
        nameField().performTextInput("General")
        composeRule.onNodeWithText(text(R.string.action_create_persona)).performClick()
        waitForText(text(R.string.channels_tab_new_uploads))
        composeRule.onNodeWithText(text(R.string.tab_channels)).assertIsSelected()
        composeRule.onNodeWithText("General").assertIsDisplayed()

        // Switch back to Guitar from Personas: opens Picks.
        composeRule.onNodeWithText("General").performClick()
        waitForText("Guitar")
        composeRule.onNodeWithText("Guitar").performClick()
        waitForText(text(R.string.placeholder_picks_title))

        // Archive Guitar (the current one): falls back to General on My channels.
        composeRule.onNodeWithText("Guitar").performClick()
        waitForDescription(text(R.string.personas_edit, "Guitar"))
        composeRule.onNodeWithContentDescription(text(R.string.personas_edit, "Guitar")).performClick()
        waitForText(text(R.string.action_archive_persona))
        composeRule.onNodeWithText(text(R.string.action_archive_persona)).performScrollTo().performClick()
        waitForText(text(R.string.channels_tab_new_uploads))
        composeRule.onNodeWithText("General").assertIsDisplayed()

        // General is now the only active persona: it can't be archived.
        composeRule.onNodeWithText("General").performClick()
        waitForText(text(R.string.personas_archived_link, 1))
        composeRule.onNodeWithContentDescription(text(R.string.personas_edit, "General")).performClick()
        waitForText(text(R.string.archive_persona_last))
        composeRule.onNodeWithText(text(R.string.action_archive_persona)).assertIsNotEnabled()
    }
}
