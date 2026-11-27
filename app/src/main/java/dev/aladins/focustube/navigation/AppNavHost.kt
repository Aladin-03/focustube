package dev.aladins.focustube.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.aladins.focustube.feature.channels.ChannelsRoute
import dev.aladins.focustube.feature.library.LibraryRoute
import dev.aladins.focustube.feature.onboarding.WelcomeRoute
import dev.aladins.focustube.feature.personas.ArchivedPersonasRoute
import dev.aladins.focustube.feature.personas.EditPersonaRoute
import dev.aladins.focustube.feature.personas.PersonasRoute
import dev.aladins.focustube.feature.picks.PicksRoute
import dev.aladins.focustube.feature.search.SearchRoute
import dev.aladins.focustube.feature.settings.SettingsRoute

private const val FADE_MILLIS = 150

/**
 * The navigation graph: every route in the app and the screen it shows.
 *
 * Switching persona is not done here: screens only change the current persona, and
 * [AppScaffold] resets navigation to the new persona's start tab.
 *
 * Back and navigate actions are wrapped in `dropUnlessResumed`, so a double tap during a
 * transition can't pop past the root and leave an empty screen.
 */
@Composable
fun AppNavHost(navController: NavHostController, startDestination: Route, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(FADE_MILLIS)) },
        exitTransition = { fadeOut(tween(FADE_MILLIS)) }
    ) {
        composable<Route.Picks> { PicksRoute() }
        composable<Route.Channels> { ChannelsRoute() }
        composable<Route.Search> { SearchRoute() }
        composable<Route.Library> { LibraryRoute() }
        composable<Route.Settings> {
            SettingsRoute(onBack = dropUnlessResumed { navController.popBackStack() })
        }
        composable<Route.Welcome> { WelcomeRoute() }
        composable<Route.Personas> {
            PersonasRoute(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onCreate = dropUnlessResumed { navController.navigate(Route.CreatePersona) },
                onEdit = { id -> navController.navigate(Route.EditPersona(id)) { launchSingleTop = true } },
                onOpenArchived = dropUnlessResumed { navController.navigate(Route.ArchivedPersonas) }
            )
        }
        composable<Route.CreatePersona> {
            EditPersonaRoute(
                onBack = dropUnlessResumed { navController.popBackStack() },
                // Pops only if this screen is still showing (creating also switches persona,
                // which may already have reset navigation).
                onDone = { navController.popBackStack<Route.CreatePersona>(inclusive = true) }
            )
        }
        composable<Route.EditPersona> {
            EditPersonaRoute(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onDone = { navController.popBackStack<Route.EditPersona>(inclusive = true) }
            )
        }
        composable<Route.ArchivedPersonas> {
            ArchivedPersonasRoute(onBack = dropUnlessResumed { navController.popBackStack() })
        }
    }
}

/**
 * Switches to a tab. Each tab keeps its own back stack: leaving a tab saves its state and coming
 * back restores it. [rootTab] is the persona's start tab, the bottom of the back stack: Back from
 * any other tab returns there, then leaves the app.
 */
fun NavController.navigateToTab(destination: TopLevelDestination, rootTab: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(rootTab.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Clears the whole back stack and opens [tab] as the new root (used when the persona changes). */
fun NavController.resetToTab(tab: TopLevelDestination) {
    navigate(tab.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
