package dev.aladins.focustube.feature.player

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.domain.model.Video

/** Everything the player (14, 15) shows. */
data class PlayerUiState(
    val videoId: String,
    val video: Video? = null,
    val personaName: String? = null,
    val isLoading: Boolean = true,
    /** Loading details failed (only possible when the video wasn't saved). */
    val error: AppError? = null,
    /** The video ended: FocusTube's own end state covers the player. */
    val hasEnded: Boolean = false,
    /** YouTube's player refused the video (not embeddable, removed...). */
    val isUnplayable: Boolean = false,
    /** Seconds played so far, kept in memory only (History and "watched" arrive in Milestone 4). */
    val positionSeconds: Float = 0f
) {
    val isShort: Boolean get() = video?.isShort == true
}
