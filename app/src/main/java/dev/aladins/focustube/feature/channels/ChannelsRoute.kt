package dev.aladins.focustube.feature.channels

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Entry point for the My channels tab. Will get its ViewModel and collect its state here once the
 * screen has real content; for now it only shows the placeholder.
 */
@Composable
fun ChannelsRoute(modifier: Modifier = Modifier) {
    ChannelsScreen(modifier = modifier)
}
