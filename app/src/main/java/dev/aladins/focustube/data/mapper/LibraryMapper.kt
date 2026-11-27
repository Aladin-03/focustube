package dev.aladins.focustube.data.mapper

import dev.aladins.focustube.data.local.entity.HistoryEntity
import dev.aladins.focustube.data.local.entity.HistoryRow
import dev.aladins.focustube.data.local.entity.WatchLaterRow
import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.WatchLaterItem
import dev.aladins.focustube.domain.model.WatchProgress
import kotlin.time.Instant

/** Watch later row (with optional History progress) -> domain. */
fun WatchLaterRow.toDomain(): WatchLaterItem = WatchLaterItem(
    video = video.toDomain(),
    addedAt = Instant.fromEpochMilliseconds(addedAt),
    progress =
        if (positionSec != null && durationSec != null && isWatched != null) {
            WatchProgress(positionSec, durationSec, isWatched)
        } else {
            null
        }
)

/** History row -> domain. */
fun HistoryRow.toDomain(): HistoryEntry = HistoryEntry(
    video = video.toDomain(),
    lastWatchedAt = Instant.fromEpochMilliseconds(lastWatchedAt),
    progress = WatchProgress(positionSec, durationSec, isWatched)
)

/** Stored History entry -> its progress. */
fun HistoryEntity.toProgress(): WatchProgress = WatchProgress(positionSec, durationSec, isWatched)
