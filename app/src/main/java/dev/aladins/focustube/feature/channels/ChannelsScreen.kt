package dev.aladins.focustube.feature.channels

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** My channels tab placeholder (Milestone 1). Stateless. */
@Composable
fun ChannelsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.placeholder_channels_title),
        message = stringResource(R.string.placeholder_channels_message),
        icon = FocusTubeIcons.Channels,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun ChannelsScreenPreview() {
    FocusTubeTheme { ChannelsScreen() }
}
