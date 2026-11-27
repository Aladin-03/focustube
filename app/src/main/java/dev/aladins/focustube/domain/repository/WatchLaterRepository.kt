package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.domain.model.WatchLaterItem
import kotlinx.coroutines.flow.Flow

/** A persona's Watch later list (stored on the phone only). */
interface WatchLaterRepository {
    /** Newest first. */
    fun observe(personaId: Long): Flow<List<WatchLaterItem>>

    fun observeIds(personaId: Long): Flow<Set<String>>

    /** Saves [videoId]; saving it again keeps the original date. */
    suspend fun add(personaId: Long, videoId: String)

    suspend fun remove(personaId: Long, videoId: String)

    /** Puts a removed item back with its original date (Undo). */
    suspend fun restore(personaId: Long, item: WatchLaterItem)
}
