package dev.aladins.focustube.feature.picks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Picks tab placeholder (Milestone 1). Stateless. */
@Composable
fun PicksScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.placeholder_picks_title),
        message = stringResource(R.string.placeholder_picks_message),
        icon = FocusTubeIcons.Picks,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun PicksScreenPreview() {
    FocusTubeTheme { PicksScreen() }
}
