package dev.aladins.focustube.feature.player

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.domain.model.Video

/** Everything the player (14, 15) shows. */
data class PlayerUiState(
    val videoId: String,
    val video: Video? = null,
    val personaName: String? = null,
    /** True until details and the saved resume position are loaded. */
    val isLoading: Boolean = true,
    /** Loading details failed (only possible when the video wasn't saved). */
    val error: AppError? = null,
    /** The video ended: FocusTube's own end state covers the player. */
    val hasEnded: Boolean = false,
    /** YouTube's player refused the video (not embeddable, removed...). */
    val isUnplayable: Boolean = false,
    /** Where playback is (starts at the saved position for a video not yet watched). */
    val positionSeconds: Float = 0f,
    /** In the current persona's Watch later. */
    val isInWatchLater: Boolean = false
) {
    val isShort: Boolean get() = video?.isShort == true
}

/** Everything the player screen can report or ask for. */
data class PlayerActions(
    val onBack: () -> Unit,
    val onProgress: (Float) -> Unit,
    val onPaused: () -> Unit,
    val onEnded: () -> Unit,
    val onReplay: () -> Unit,
    val onUnplayable: () -> Unit,
    val onToggleWatchLater: () -> Unit
)
