package dev.aladins.focustube.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for the Library tab (16). */
@Composable
fun LibraryRoute(
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        actions =
            LibraryActions(
                onTabChange = viewModel::onTabChange,
                onOpenVideo = onOpenVideo,
                onRemoveFromWatchLater = viewModel::onRemoveFromWatchLater,
                onRemoveFromHistory = viewModel::onRemoveFromHistory,
                onUndo = viewModel::onUndo,
                onUndoDismissed = viewModel::onUndoDismissed,
                onClearHistory = viewModel::onClearHistory
            ),
        modifier = modifier
    )
}
