package dev.aladins.focustube.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.domain.model.Persona

/** The four tabs shown in the bottom bar (or the rail on wide screens), in display order. */
enum class TopLevelDestination(val route: Route, @DrawableRes val icon: Int, @StringRes val label: Int) {
    PICKS(Route.Picks, FocusTubeIcons.Picks, R.string.tab_picks),
    CHANNELS(Route.Channels, FocusTubeIcons.Channels, R.string.tab_channels),
    SEARCH(Route.Search, FocusTubeIcons.Search, R.string.tab_search),
    LIBRARY(Route.Library, FocusTubeIcons.Library, R.string.tab_library)
}

/** The tab a persona opens on: Picks if it has a goal, My channels if not (REQUIREMENTS.md). */
fun Persona.startTab(): TopLevelDestination = if (hasGoal) TopLevelDestination.PICKS else TopLevelDestination.CHANNELS
