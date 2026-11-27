package dev.aladins.focustube.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for Settings, opened from the gear in the top bar. */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenArchived: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onBack = onBack,
        onDefaultRegionsApply = viewModel::onDefaultRegionsApply,
        onOpenArchived = onOpenArchived,
        modifier = modifier
    )
}
