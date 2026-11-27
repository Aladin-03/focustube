package dev.aladins.focustube.navigation

import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import org.junit.Assert.assertEquals
import org.junit.Test

/** The four tabs, their order, routes, icons and labels (REQUIREMENTS.md, DESIGN.md). */
class TopLevelDestinationTest {
    @Test
    fun fourTabsInDesignOrder() {
        assertEquals(
            listOf(Route.Picks, Route.Channels, Route.Search, Route.Library),
            TopLevelDestination.entries.map { it.route }
        )
    }

    @Test
    fun iconsMatchDesign() {
        assertEquals(
            listOf(FocusTubeIcons.Picks, FocusTubeIcons.Channels, FocusTubeIcons.Search, FocusTubeIcons.Library),
            TopLevelDestination.entries.map { it.icon }
        )
    }

    @Test
    fun labelsMatchDesign() {
        assertEquals(
            listOf(R.string.tab_picks, R.string.tab_channels, R.string.tab_search, R.string.tab_library),
            TopLevelDestination.entries.map { it.label }
        )
    }

    @Test
    fun settingsIsNotATab() {
        assertEquals(false, TopLevelDestination.entries.any { it.route == Route.Settings })
    }
}
