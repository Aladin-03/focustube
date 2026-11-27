package dev.aladins.focustube.data.repository

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.local.dao.SearchDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.database.TransactionRunner
import dev.aladins.focustube.data.local.entity.SearchCacheEntity
import dev.aladins.focustube.data.local.entity.SearchResultEntity
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.data.mapper.toEntity
import dev.aladins.focustube.data.mapper.toRegions
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.SavedSearch
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import dev.aladins.focustube.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Classic search, cache first. Page 1 is fetched only when nothing usable is saved for the key, or
 * on Refresh. Each page is one search.list call (100 units) for one region, plus one videos.list
 * call (1 unit) for the details. Page 1 uses the persona's first region; "More results" takes the
 * other regions in turn, then their next pages. Saves older than 30 days count as missing.
 */
@Singleton
class DefaultSearchRepository @Inject constructor(
    private val transaction: TransactionRunner,
    private val searchDao: SearchDao,
    private val videoDao: VideoDao,
    private val youTube: YouTubeDataSource,
    private val json: Json,
    private val clock: Clock
) : SearchRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(key: SearchKey): Flow<SavedSearch?> =
        searchDao.observeCache(key.personaId, key.normalizedQuery, key.mode.stored, key.regionsKey)
            .flatMapLatest { cache ->
                if (cache == null || cache.isStale()) {
                    flowOf(null)
                } else {
                    searchDao.observeResultVideos(cache.id).map { rows ->
                        SavedSearch(
                            query = cache.query,
                            videos = rows.map { it.toDomain() },
                            savedAt = Instant.fromEpochMilliseconds(cache.createdAt),
                            hasMore = nextPage(cache) != null
                        )
                    }
                }
            }

    override suspend fun search(key: SearchKey, refresh: Boolean): AppResult<Unit> {
        val existing = searchDao.getCache(key.personaId, key.normalizedQuery, key.mode.stored, key.regionsKey)
        val now = clock.now().toEpochMilliseconds()
        if (!refresh && existing != null && !existing.isStale()) {
            searchDao.touch(existing.id, now)
            return AppResult.Success(Unit)
        }

        val region = key.regions.first().code
        val page = fetchPage(key.query, region, pageToken = null)
        if (page is AppResult.Failure) return page
        val (videoIds, nextToken) = (page as AppResult.Success).value

        transaction {
            val cache =
                SearchCacheEntity(
                    id = existing?.id ?: 0,
                    personaId = key.personaId,
                    query = key.query.trim(),
                    queryNormalized = key.normalizedQuery,
                    mode = key.mode.stored,
                    regions = key.regionsKey,
                    regionOrder = key.regions.joinToString(",") { it.code },
                    pagesLoaded = 1,
                    pageTokens = encodeTokens(mapOf(region to nextToken)),
                    createdAt = now,
                    lastUsedAt = now
                )
            val cacheId = if (existing ==
                null
            ) {
                searchDao.insertCache(cache)
            } else {
                cache.id.also { searchDao.updateCache(cache) }
            }
            searchDao.replaceResults(
                cacheId,
                videoIds.mapIndexed { i, id ->
                    SearchResultEntity(cacheId, id, rank = i)
                }
            )
        }
        return AppResult.Success(Unit)
    }

    override suspend fun loadMore(key: SearchKey): AppResult<Unit> {
        val cache =
            searchDao.getCache(key.personaId, key.normalizedQuery, key.mode.stored, key.regionsKey)
                ?: return search(key, refresh = false)
        val (region, token) = nextPage(cache) ?: return AppResult.Success(Unit)

        val page = fetchPage(cache.query, region, token)
        if (page is AppResult.Failure) return page
        val (videoIds, nextToken) = (page as AppResult.Success).value

        transaction {
            val known = searchDao.resultVideoIds(cache.id).toSet()
            val firstRank = searchDao.maxRank(cache.id) + 1
            val fresh = videoIds.filterNot { it in known }
            searchDao.insertResults(
                fresh.mapIndexed { i, id ->
                    SearchResultEntity(cache.id, id, rank = firstRank + i)
                }
            )
            val tokens = decodeTokens(cache.pageTokens).toMutableMap().apply { put(region, nextToken) }
            searchDao.updateCache(
                cache.copy(
                    pagesLoaded = cache.pagesLoaded + 1,
                    pageTokens = encodeTokens(tokens),
                    lastUsedAt = clock.now().toEpochMilliseconds()
                )
            )
        }
        return AppResult.Success(Unit)
    }

    override fun observeRecent(personaId: Long, mode: SearchMode): Flow<List<RecentSearch>> =
        searchDao.observeRecent(personaId, mode.stored, RECENT_LIMIT).map { rows ->
            rows.filterNot { it.isStale() }.map {
                RecentSearch(it.query, it.regionOrder.toRegions(), Instant.fromEpochMilliseconds(it.lastUsedAt))
            }
        }

    override suspend fun remove(key: SearchKey) {
        val cache = searchDao.getCache(key.personaId, key.normalizedQuery, key.mode.stored, key.regionsKey) ?: return
        searchDao.deleteCache(cache.id)
    }

    override suspend fun removeStale() {
        val cutoff = (clock.now() - MAX_AGE).toEpochMilliseconds()
        transaction {
            searchDao.deleteOlderThan(cutoff)
            videoDao.deleteUnusedOlderThan(cutoff)
        }
    }

    /** search.list then videos.list; saves the details. Returns ids in order and the next token. */
    private suspend fun fetchPage(
        query: String,
        region: String,
        pageToken: String?
    ): AppResult<Pair<List<String>, String?>> {
        val search = youTube.search(query.trim(), region, pageToken)
        if (search is AppResult.Failure) return search
        val response = (search as AppResult.Success).value
        val ids = response.items.mapNotNull { it.id.videoId }.distinct()
        if (ids.isEmpty()) return AppResult.Success(emptyList<String>() to response.nextPageToken)

        val details = youTube.videos(ids)
        if (details is AppResult.Failure) return details
        val dtos = (details as AppResult.Success).value
        videoDao.upsertAll(dtos.map { it.toEntity(clock.now()) })
        // Keep YouTube's order; drop ids it returned no details for (removed or private).
        val withDetails = dtos.map { it.id }.toSet()
        return AppResult.Success(ids.filter { it in withDetails } to response.nextPageToken)
    }

    /**
     * The next page to load: regions not searched yet come first, in the persona's order, then
     * regions that still have a next page, in turn. Null when nothing is left.
     */
    private fun nextPage(cache: SearchCacheEntity): Pair<String, String?>? {
        val order = cache.regionOrder.split(",").filter { it.isNotBlank() }
        val tokens = decodeTokens(cache.pageTokens)
        order.firstOrNull { it !in tokens }?.let { return it to null }
        if (order.isEmpty()) return null
        val start = cache.pagesLoaded % order.size
        val rotated = order.drop(start) + order.take(start)
        return rotated.firstOrNull { tokens[it] != null }?.let { it to tokens[it] }
    }

    private fun SearchCacheEntity.isStale(): Boolean = clock.now() - Instant.fromEpochMilliseconds(createdAt) > MAX_AGE

    private fun encodeTokens(tokens: Map<String, String?>): String = json.encodeToString(tokens)

    private fun decodeTokens(stored: String): Map<String, String?> = runCatching {
        json.decodeFromString<Map<String, String?>>(stored)
    }.getOrDefault(emptyMap())

    private val SearchMode.stored: String get() = name.uppercase()

    private companion object {
        val MAX_AGE = 30.days
        const val RECENT_LIMIT = 10
    }
}
