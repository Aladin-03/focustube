package dev.aladins.focustube.navigation

import dev.aladins.focustube.testing.persona
import org.junit.Assert.assertEquals
import org.junit.Test

/** REQUIREMENTS.md: a persona with a goal opens on Picks, one without on My channels. */
class StartTabTest {
    @Test
    fun goalOpensPicks() {
        assertEquals(TopLevelDestination.PICKS, persona(1, "Guitar", goal = "Learn").startTab())
    }

    @Test
    fun noGoalOpensChannels() {
        assertEquals(TopLevelDestination.CHANNELS, persona(1, "General").startTab())
        assertEquals(TopLevelDestination.CHANNELS, persona(1, "General", goal = "  ").startTab())
    }
}
