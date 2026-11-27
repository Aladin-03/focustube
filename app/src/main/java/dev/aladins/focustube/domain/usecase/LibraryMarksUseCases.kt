package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.repository.HistoryRepository
import dev.aladins.focustube.domain.repository.WatchLaterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/** What video lists show per video for a persona: saved to Watch later, and watch progress. */
data class LibraryMarks(val savedIds: Set<String> = emptySet(), val progress: Map<String, WatchProgress> = emptyMap())

/** The persona's Watch later ids and watch progress, for bookmarks and progress bars on rows. */
class ObserveLibraryMarksUseCase @Inject constructor(
    private val watchLater: WatchLaterRepository,
    private val history: HistoryRepository
) {
    operator fun invoke(personaId: Long): Flow<LibraryMarks> =
        combine(watchLater.observeIds(personaId), history.observeProgress(personaId), ::LibraryMarks)
}

/** Saves a video to the persona's Watch later, or removes it if already saved. */
class ToggleWatchLaterUseCase @Inject constructor(private val watchLater: WatchLaterRepository) {
    suspend operator fun invoke(personaId: Long, videoId: String) {
        if (videoId in watchLater.observeIds(personaId).first()) {
            watchLater.remove(personaId, videoId)
        } else {
            watchLater.add(personaId, videoId)
        }
    }
}
