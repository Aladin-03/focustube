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
import androidx.compose.runtime.getValue
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
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * The app shell. On a tab it shows the top bar and the tabs: a bottom bar on narrow screens, a
 * navigation rail on the left from medium width up (tablets, phones in landscape). Full-screen
 * routes such as Settings draw their own top bar and hide the tabs.
 *
 * Content (top bar included) is centred and never wider than
 * [FocusTubeTheme.spacing.contentMaxWidth]. The NavHost stays in one place in the tree so
 * screens keep their state when the bars appear or disappear.
 */
@Composable
fun AppScaffold(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = currentDestination?.topLevelDestination()
    // Before the first destination is known, assume a tab so the bars don't flicker in.
    val showTabs = currentDestination == null || currentTab != null
    val useRail =
        currentWindowAdaptiveInfoV2().windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val onTabClick: (TopLevelDestination) -> Unit = { navController.navigateToTab(it) }

    Row(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (showTabs && useRail) {
            FtNavRail(
                modifier =
                    Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical)
                    )
            ) {
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
                        FtTopBar(
                            title = stringResource(R.string.app_name),
                            actions = {
                                IconButton(onClick = { navController.navigate(Route.Settings) }) {
                                    Icon(
                                        painter = painterResource(FocusTubeIcons.Settings),
                                        contentDescription = stringResource(R.string.action_settings),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        )
                    }
                    AppNavHost(
                        navController = navController,
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

/** The tab this destination belongs to, or null for full-screen routes such as Settings. */
private fun NavDestination.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { tab ->
        hierarchy.any { it.hasRoute(tab.route::class) }
    }
