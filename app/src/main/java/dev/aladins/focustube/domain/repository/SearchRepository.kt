package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.SavedSearch
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import kotlinx.coroutines.flow.Flow

/**
 * Saved searches. Cache first: [search] calls YouTube only when nothing usable is saved for the
 * key, or when [search] is asked to refresh.
 */
interface SearchRepository {
    /** The saved search for [key], or null if there is none. */
    fun observe(key: SearchKey): Flow<SavedSearch?>

    /** Makes sure [key] has saved results; fetches page 1 only if missing, stale or [refresh]. */
    suspend fun search(key: SearchKey, refresh: Boolean): AppResult<Unit>

    /** Loads the next page, taking the persona's regions in turn. */
    suspend fun loadMore(key: SearchKey): AppResult<Unit>

    /** The persona's saved searches for [mode], most recently used first. */
    fun observeRecent(personaId: Long, mode: SearchMode): Flow<List<RecentSearch>>

    /** Removes a saved search (it is only a cache). */
    suspend fun remove(key: SearchKey)

    /** Drops saved searches and unused video details older than 30 days (YouTube API policy). */
    suspend fun removeStale()
}
