package dev.aladins.focustube.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.aladins.focustube.feature.channels.ChannelsRoute
import dev.aladins.focustube.feature.library.LibraryRoute
import dev.aladins.focustube.feature.picks.PicksRoute
import dev.aladins.focustube.feature.search.SearchRoute
import dev.aladins.focustube.feature.settings.SettingsRoute

private const val FADE_MILLIS = 150

/** The navigation graph: every route in the app and the screen it shows. */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Route.Picks,
        modifier = modifier,
        enterTransition = { fadeIn(tween(FADE_MILLIS)) },
        exitTransition = { fadeOut(tween(FADE_MILLIS)) }
    ) {
        composable<Route.Picks> { PicksRoute() }
        composable<Route.Channels> { ChannelsRoute() }
        composable<Route.Search> { SearchRoute() }
        composable<Route.Library> { LibraryRoute() }
        composable<Route.Settings> { SettingsRoute(onBack = navController::popBackStack) }
    }
}

/**
 * Switches to a tab. Each tab keeps its own back stack: leaving a tab saves its state and coming
 * back restores it. Back from any tab returns to the start tab, then leaves the app.
 */
fun NavController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
