package dev.aladins.focustube.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Entry point for the Library tab. Will get its ViewModel and collect its state here once the
 * screen has real content; for now it only shows the placeholder.
 */
@Composable
fun LibraryRoute(modifier: Modifier = Modifier) {
    LibraryScreen(modifier = modifier)
}
