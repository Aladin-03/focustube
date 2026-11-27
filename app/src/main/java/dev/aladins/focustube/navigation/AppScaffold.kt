package dev.aladins.focustube.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.FtBottomBar
import dev.aladins.focustube.core.designsystem.components.FtBottomBarItem
import dev.aladins.focustube.core.designsystem.components.FtNavRail
import dev.aladins.focustube.core.designsystem.components.FtNavRailItem
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.PersonaAvatar
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.Persona

/**
 * The app shell. On a tab it shows the persona top bar and the tabs: a bottom bar on narrow
 * screens, a navigation rail on the left from medium width up. Full-screen routes (Welcome,
 * Personas, Settings...) draw their own top bar and hide the tabs.
 *
 * Starts on Welcome when there is no persona ([AppUiState.NeedsWelcome]), otherwise on the
 * persona's start tab. Whenever the current persona changes (created, switched, or the current
 * one archived), the back stack is cleared and the new persona's start tab becomes its root.
 *
 * Content is centred and never wider than [FocusTubeTheme.spacing.contentMaxWidth]. The NavHost
 * stays in one place in the tree so screens keep their state when the bars appear or disappear.
 *
 * @param appState never [AppUiState.Loading]: the splash screen covers that.
 */
@Composable
fun AppScaffold(
    appState: AppUiState,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val persona = (appState as? AppUiState.Ready)?.persona
    val startDestination: Route = remember { persona?.startTab()?.route ?: Route.Welcome }
    var rootTab by rememberSaveable { mutableStateOf(persona?.startTab() ?: TopLevelDestination.PICKS) }
    var shownPersonaId by rememberSaveable { mutableStateOf(persona?.id) }

    LaunchedEffect(persona?.id) {
        if (persona != null && persona.id != shownPersonaId) {
            shownPersonaId = persona.id
            rootTab = persona.startTab()
            navController.resetToTab(rootTab)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = currentDestination?.topLevelDestination()
    // Before the first destination is known, follow the start destination so bars don't flicker.
    val showTabs =
        persona != null && (currentTab != null || (currentDestination == null && startDestination != Route.Welcome))
    val useRail =
        currentWindowAdaptiveInfoV2().windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val onTabClick: (TopLevelDestination) -> Unit = { navController.navigateToTab(it, rootTab) }

    Row(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (showTabs && useRail) {
            FtNavRail {
                TopLevelDestination.entries.forEach { tab ->
                    FtNavRailItem(
                        selected = tab == currentTab,
                        onClick = { onTabClick(tab) },
                        icon = painterResource(tab.icon),
                        label = stringResource(tab.label)
                    )
                }
            }
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .then(
                        if (showTabs && useRail) {
                            Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        } else {
                            Modifier
                        }
                    )
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(Modifier.widthIn(max = FocusTubeTheme.spacing.contentMaxWidth).fillMaxSize()) {
                    if (showTabs) {
                        PersonaTopBar(
                            persona = persona,
                            onPersonaClick = { navController.navigate(Route.Personas) { launchSingleTop = true } },
                            onSettingsClick = { navController.navigate(Route.Settings) { launchSingleTop = true } }
                        )
                    }
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier =
                            Modifier
                                .weight(1f)
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        if (showTabs && !useRail) {
                                            WindowInsetsSides.Horizontal
                                        } else {
                                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                                        }
                                    )
                                )
                    )
                }
            }
            if (showTabs && !useRail) {
                FtBottomBar {
                    TopLevelDestination.entries.forEach { tab ->
                        FtBottomBarItem(
                            selected = tab == currentTab,
                            onClick = { onTabClick(tab) },
                            icon = painterResource(tab.icon),
                            label = stringResource(tab.label)
                        )
                    }
                }
            }
        }
    }
}

/** Top bar on the tabs: persona avatar, name and chevron (opens Personas), and the Settings gear. */
@Composable
private fun PersonaTopBar(persona: Persona, onPersonaClick: () -> Unit, onSettingsClick: () -> Unit) {
    FtTopBar(
        title = persona.name,
        leading = { PersonaAvatar(name = persona.name) },
        onTitleClick = onPersonaClick,
        titleClickLabel = stringResource(R.string.action_switch_persona),
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(FocusTubeIcons.Settings),
                    contentDescription = stringResource(R.string.action_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

/** The tab this destination belongs to, or null for full-screen routes. */
private fun NavDestination.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { tab ->
        hierarchy.any { it.hasRoute(tab.route::class) }
    }
