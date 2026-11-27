package dev.aladins.focustube.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Library tab placeholder (Milestone 1). Stateless. */
@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.placeholder_library_title),
        message = stringResource(R.string.placeholder_library_message),
        icon = FocusTubeIcons.Library,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun LibraryScreenPreview() {
    FocusTubeTheme { LibraryScreen() }
}
