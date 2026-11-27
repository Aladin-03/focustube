package dev.aladins.focustube.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Entry point for the Search tab. Will get its ViewModel and collect its state here once the
 * screen has real content; for now it only shows the placeholder.
 */
@Composable
fun SearchRoute(modifier: Modifier = Modifier) {
    SearchScreen(modifier = modifier)
}
