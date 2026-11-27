package dev.aladins.focustube.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Search tab placeholder (Milestone 1). Stateless. */
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.placeholder_search_title),
        message = stringResource(R.string.placeholder_search_message),
        icon = FocusTubeIcons.Search,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun SearchScreenPreview() {
    FocusTubeTheme { SearchScreen() }
}
