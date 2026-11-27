package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.WatchProgress
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/** A persona's History: one entry per video, with progress and whether it was watched. */
interface HistoryRepository {
    /** Newest first. */
    fun observe(personaId: Long): Flow<List<HistoryEntry>>

    /** Progress of every video this persona has started, by video id (progress bars). */
    fun observeProgress(personaId: Long): Flow<Map<String, WatchProgress>>

    /** Ids of watched videos: picks and AI search leave these out (Milestones 6 and 7). */
    fun observeWatchedIds(personaId: Long): Flow<Set<String>>

    suspend fun getProgress(personaId: Long, videoId: String): WatchProgress?

    /** Saves progress at [at]. Watched never goes back to unwatched. */
    suspend fun save(personaId: Long, videoId: String, progress: WatchProgress, at: Instant)

    suspend fun remove(personaId: Long, videoId: String)

    /** Puts a removed entry back exactly as it was (Undo). */
    suspend fun restore(personaId: Long, entry: HistoryEntry)

    /** Removes the persona's whole History (asked to confirm first; no Undo). */
    suspend fun clear(personaId: Long)
}
