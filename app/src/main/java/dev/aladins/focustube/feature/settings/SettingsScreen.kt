package dev.aladins.focustube.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Settings placeholder (Milestone 1): its own top bar with Back, no tabs. Stateless. */
@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FtTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        EmptyState(
            title = stringResource(R.string.placeholder_settings_title),
            message = stringResource(R.string.placeholder_settings_message),
            icon = FocusTubeIcons.Settings,
            modifier = Modifier.weight(1f)
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    FocusTubeTheme { SettingsScreen(onBack = {}) }
}
