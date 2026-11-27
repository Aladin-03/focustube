package dev.aladins.focustube.feature.picks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Entry point for the Picks tab. Will get its ViewModel and collect its state here once the
 * screen has real content; for now it only shows the placeholder.
 */
@Composable
fun PicksRoute(modifier: Modifier = Modifier) {
    PicksScreen(modifier = modifier)
}
