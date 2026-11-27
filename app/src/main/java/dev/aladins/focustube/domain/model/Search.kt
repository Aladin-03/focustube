package dev.aladins.focustube.domain.model

import kotlin.time.Instant

/** Search modes. AI search arrives in Milestone 7. */
enum class SearchMode { Classic, Ai }

/** Results filter, applied to saved results on the phone (costs nothing). */
enum class ShortsFilter {
    All,
    Videos,
    Shorts
    ;

    fun apply(videos: List<Video>): List<Video> = when (this) {
        All -> videos
        Videos -> videos.filterNot { it.isShort }
        Shorts -> videos.filter { it.isShort }
    }
}

/**
 * What identifies a saved search (REQUIREMENTS.md): persona, query, mode and regions.
 * The query is compared normalised; regions as a set. The Shorts filter is not part of it.
 */
data class SearchKey(
    val personaId: Long,
    val query: String,
    val mode: SearchMode,
    /** In the persona's order: page 1 uses the first region. */
    val regions: List<Region>
) {
    val normalizedQuery: String get() = normalizeQuery(query)

    /** Regions as stored in the cache key: sorted, so order doesn't create a second search. */
    val regionsKey: String get() = regions.map { it.code }.sorted().joinToString(",")

    companion object {
        /** Trimmed, inner spaces collapsed, lower case. */
        fun normalizeQuery(query: String): String = query.trim().replace(Regex("\\s+"), " ").lowercase()
    }
}

/** A saved search with its results in order. */
data class SavedSearch(
    val query: String,
    val videos: List<Video>,
    val savedAt: Instant,
    /** "More results" can load another page. */
    val hasMore: Boolean
)

/** A saved search listed under "Recent" when the search field is empty. */
data class RecentSearch(val query: String, val regions: List<Region>, val lastUsedAt: Instant)

/** Today's YouTube usage from this phone, and when the shared daily limit resets. */
data class QuotaStatus(
    val usedToday: Long,
    val dailyLimit: Long,
    val resetsAt: Instant,
    /** Used up: no new YouTube requests until [resetsAt]. */
    val isExhausted: Boolean
)
