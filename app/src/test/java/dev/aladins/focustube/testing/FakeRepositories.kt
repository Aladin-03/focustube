package dev.aladins.focustube.testing

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.SavedSearch
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.repository.QuotaRepository
import dev.aladins.focustube.domain.repository.SearchRepository
import dev.aladins.focustube.domain.repository.SettingsRepository
import dev.aladins.focustube.domain.repository.VideoRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Settings with default regions in memory. */
class FakeSettingsRepository(initial: List<Region> = Region.Defaults) : SettingsRepository {
    private val regions = MutableStateFlow(initial)

    override fun observeDefaultRegions(): Flow<List<Region>> = regions

    override suspend fun setDefaultRegions(regions: List<Region>) {
        this.regions.value = regions
    }
}

/** Quota status the test sets directly. */
class FakeQuotaRepository(used: Long = 0) : QuotaRepository {
    val status = MutableStateFlow(
        QuotaStatus(
            used,
            10_000,
            Instant.fromEpochMilliseconds(0),
            isExhausted =
                used >= 10_000
        )
    )

    fun exhaust() = status.update { it.copy(usedToday = 10_000, isExhausted = true) }

    override fun observeToday(): Flow<QuotaStatus> = status
}

/**
 * Search repository the test scripts: [results] are what a fetch "finds", [nextResult] is what
 * search/loadMore return. Records every call.
 */
class FakeSearchRepository : SearchRepository {
    private val saved = MutableStateFlow<Map<SearchKey, SavedSearch>>(emptyMap())
    val searchCalls = mutableListOf<Pair<SearchKey, Boolean>>()
    val loadMoreCalls = mutableListOf<SearchKey>()
    val removed = mutableListOf<SearchKey>()
    var results: List<Video> = emptyList()
    var nextResult: AppResult<Unit> = AppResult.Success(Unit)
    var recent = MutableStateFlow<List<RecentSearch>>(emptyList())

    /** Saved searches are matched like the real cache key. */
    private fun SearchKey.id() = SearchKey(personaId, normalizedQuery, mode, regions.sortedBy { it.code })

    fun save(key: SearchKey, videos: List<Video>, hasMore: Boolean = false) =
        saved.update { it + (key.id() to SavedSearch(key.query, videos, Instant.fromEpochMilliseconds(0), hasMore)) }

    override fun observe(key: SearchKey): Flow<SavedSearch?> = saved.map { it[key.id()] }

    override suspend fun search(key: SearchKey, refresh: Boolean): AppResult<Unit> {
        searchCalls += key to refresh
        if (nextResult is AppResult.Success && (refresh || key.id() !in saved.value)) save(key, results)
        return nextResult
    }

    override suspend fun loadMore(key: SearchKey): AppResult<Unit> {
        loadMoreCalls += key
        return nextResult
    }

    override fun observeRecent(personaId: Long, mode: SearchMode): Flow<List<RecentSearch>> = recent

    override suspend fun remove(key: SearchKey) {
        removed += key
    }

    override suspend fun removeStale() = Unit
}

/** Video repository over a map; [fetchCalls] counts fetches of missing videos. */
class FakeVideoRepository(initial: Map<String, Video> = emptyMap()) : VideoRepository {
    val videos = MutableStateFlow(initial)
    var fetchCalls = 0
    var fetchResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun observe(id: String): Flow<Video?> = videos.map { it[id] }

    override suspend fun fetchIfMissing(id: String): AppResult<Unit> {
        if (id in videos.value) return AppResult.Success(Unit)
        fetchCalls++
        return fetchResult
    }

    var refreshCalls = 0

    override suspend fun refreshStaleLibraryVideos(): AppResult<Unit> {
        refreshCalls++
        return AppResult.Success(Unit)
    }
}

/** A test video. */
fun video(id: String, isShort: Boolean = false) = Video(
    id = id,
    title = "Video $id",
    channelId = "channel-1",
    channelTitle = "Fingerstyle Fred",
    durationSeconds = if (isShort) 45 else 664,
    publishedAt = Instant.parse("2026-11-01T10:00:00Z"),
    viewCount = 84_000,
    isShort = isShort,
    description = "About $id",
    thumbnailUrl = null
)
