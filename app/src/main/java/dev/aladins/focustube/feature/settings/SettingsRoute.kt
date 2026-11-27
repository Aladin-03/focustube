package dev.aladins.focustube.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Entry point for Settings, opened from the gear in the top bar. */
@Composable
fun SettingsRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    SettingsScreen(onBack = onBack, modifier = modifier)
}
