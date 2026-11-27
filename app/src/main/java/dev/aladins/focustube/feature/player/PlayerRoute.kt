package dev.aladins.focustube.feature.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for the player (14, 15). Saves progress when the screen stops (leaving or background). */
@Composable
fun PlayerRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: PlayerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onLeave() }
    PlayerScreen(
        state = state,
        actions =
            PlayerActions(
                onBack = onBack,
                onProgress = viewModel::onProgress,
                onPaused = viewModel::onPaused,
                onEnded = viewModel::onEnded,
                onReplay = viewModel::onReplay,
                onUnplayable = viewModel::onUnplayable,
                onToggleWatchLater = viewModel::onToggleWatchLater
            ),
        modifier = modifier
    )
}
