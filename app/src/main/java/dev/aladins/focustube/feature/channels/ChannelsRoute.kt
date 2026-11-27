package dev.aladins.focustube.feature.channels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for My channels (08). */
@Composable
fun ChannelsRoute(
    onOpenVideo: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onAddChannel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChannelsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChannelsScreen(
        state = state,
        actions =
            ChannelsActions(
                onTabChange = viewModel::onTabChange,
                onCheckNow = viewModel::onCheckNow,
                onOpenVideo = onOpenVideo,
                onOpenChannel = onOpenChannel,
                onAddChannel = onAddChannel,
                onToggleWatchLater = viewModel::onToggleWatchLater,
                onUnfollow = viewModel::onUnfollow,
                onUndo = viewModel::onUndo,
                onUndoDismissed = viewModel::onUndoDismissed
            ),
        modifier = modifier
    )
}
