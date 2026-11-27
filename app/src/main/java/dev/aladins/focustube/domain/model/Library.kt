package dev.aladins.focustube.domain.model

import kotlin.time.Instant

/** How far a persona got in a video. */
data class WatchProgress(
    val positionSeconds: Long,
    val durationSeconds: Long,
    /** Watched (past half, or to the end for a Short). Once true it stays true. */
    val isWatched: Boolean
) {
    /** 0..1 for the thumbnail's progress bar; a watched video shows as full. */
    val fraction: Float
        get() = when {
            isWatched -> 1f
            durationSeconds <= 0 -> 0f
            else -> (positionSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f)
        }
}

/** A video in a persona's Watch later list. */
data class WatchLaterItem(
    val video: Video,
    val addedAt: Instant,
    /** Set if the persona has started or watched it. */
    val progress: WatchProgress?
)

/** A video in a persona's History (one entry per video; re-watching updates it). */
data class HistoryEntry(val video: Video, val lastWatchedAt: Instant, val progress: WatchProgress)
