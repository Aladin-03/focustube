package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.repository.HistoryRepository
import javax.inject.Inject
import kotlin.time.Clock

/**
 * Saves how far a persona got in a video and whether it now counts as watched
 * (REQUIREMENTS.md): past half its length, or to the end for a Short. Once watched, always
 * watched; re-watching only updates the position and time.
 */
class MarkWatchedUseCase @Inject constructor(private val history: HistoryRepository, private val clock: Clock) {
    suspend operator fun invoke(
        personaId: Long,
        videoId: String,
        positionSeconds: Long,
        durationSeconds: Long,
        isShort: Boolean,
        ended: Boolean
    ) {
        val before = history.getProgress(personaId, videoId)
        val watched = before?.isWatched == true || isWatched(positionSeconds, durationSeconds, isShort, ended)
        val position = if (ended) durationSeconds else positionSeconds
        history.save(personaId, videoId, WatchProgress(position, durationSeconds, watched), clock.now())
    }

    companion object {
        /** The watched rule on its own (also used by the player to save at the moment it flips). */
        fun isWatched(positionSeconds: Long, durationSeconds: Long, isShort: Boolean, ended: Boolean): Boolean =
            ended || (!isShort && durationSeconds > 0 && positionSeconds * 2 >= durationSeconds)
    }
}
