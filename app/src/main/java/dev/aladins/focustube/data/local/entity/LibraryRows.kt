package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * A Watch later row joined with its video and (if started) its History progress. History's
 * duration_sec is aliased so it doesn't clash with the video's.
 */
data class WatchLaterRow(
    @Embedded val video: VideoEntity,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "position_sec") val positionSec: Long?,
    @ColumnInfo(name = "history_duration_sec") val durationSec: Long?,
    @ColumnInfo(name = "is_watched") val isWatched: Boolean?
)

/** A History row joined with its video. */
data class HistoryRow(
    @Embedded val video: VideoEntity,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long,
    @ColumnInfo(name = "position_sec") val positionSec: Long,
    @ColumnInfo(name = "history_duration_sec") val durationSec: Long,
    @ColumnInfo(name = "is_watched") val isWatched: Boolean
)
