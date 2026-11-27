package dev.aladins.focustube.feature.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for the player (14, 15). */
@Composable
fun PlayerRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: PlayerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        state = state,
        onBack = onBack,
        onProgress = viewModel::onProgress,
        onEnded = viewModel::onEnded,
        onReplay = viewModel::onReplay,
        onUnplayable = viewModel::onUnplayable,
        modifier = modifier
    )
}
