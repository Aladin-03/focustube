package dev.aladins.focustube.testing

import dev.aladins.focustube.data.local.dao.QuotaDao
import dev.aladins.focustube.data.local.dao.SearchDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.database.TransactionRunner
import dev.aladins.focustube.data.local.entity.SearchCacheEntity
import dev.aladins.focustube.data.local.entity.SearchResultEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Runs the block directly (no database). */
object DirectTransactionRunner : TransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T = block()
}

/** In-memory quota_log. */
class FakeQuotaDao : QuotaDao {
    val rows = MutableStateFlow<Map<String, Long>>(emptyMap())

    override fun observeUnits(date: String): Flow<Long> = rows.map { it[date] ?: 0 }

    override fun unitsBlocking(date: String): Long = rows.value[date] ?: 0

    override fun ensureRowBlocking(date: String) = rows.update { if (date in it) it else it + (date to 0L) }

    override fun incrementBlocking(date: String, units: Long) =
        rows.update { m -> m[date]?.let { m + (date to it + units) } ?: m }

    override fun raiseToAtLeastBlocking(date: String, units: Long) =
        rows.update { m -> m[date]?.let { m + (date to maxOf(it, units)) } ?: m }

    override fun setBlocking(date: String, units: Long) = rows.update { m -> if (date in m) m + (date to units) else m }
}

/** In-memory video table. */
class FakeVideoDao : VideoDao {
    val rows = MutableStateFlow<Map<String, VideoEntity>>(emptyMap())

    /** Ids referenced by saved searches (set by [FakeSearchDao]) for the cleanup query. */
    var referencedIds: () -> Set<String> = { emptySet() }

    override suspend fun upsertAll(videos: List<VideoEntity>) = rows.update { m ->
        m +
            videos.associateBy { it.youtubeId }
    }

    override fun observe(id: String): Flow<VideoEntity?> = rows.map { it[id] }

    override suspend fun get(id: String): VideoEntity? = rows.value[id]

    /** Library ids (set by the test) for [libraryIdsFetchedBefore]. */
    var libraryIds: Set<String> = emptySet()

    override suspend fun libraryIdsFetchedBefore(cutoff: Long): List<String> = rows.value.values.filter {
        it.fetchedAt <
            cutoff &&
            it.youtubeId in libraryIds
    }.map { it.youtubeId }

    override suspend fun deleteUnusedOlderThan(cutoff: Long): Int {
        val keep = referencedIds() + libraryIds
        val drop = rows.value.values.filter {
            it.fetchedAt < cutoff && it.youtubeId !in keep
        }.map { it.youtubeId }.toSet()
        rows.update { m -> m - drop }
        return drop.size
    }
}

/** In-memory search_cache + search_result, with the same queries as SearchDao's SQL. */
class FakeSearchDao(private val videos: FakeVideoDao) : SearchDao {
    val caches = MutableStateFlow<Map<Long, SearchCacheEntity>>(emptyMap())
    val results = MutableStateFlow<List<SearchResultEntity>>(emptyList())
    private var nextId = 1L

    init {
        videos.referencedIds = { results.value.map { it.videoId }.toSet() }
    }

    private fun match(c: SearchCacheEntity, personaId: Long, query: String, mode: String, regions: String) =
        c.personaId == personaId && c.queryNormalized == query && c.mode == mode && c.regions == regions

    override fun observeCache(personaId: Long, query: String, mode: String, regions: String): Flow<SearchCacheEntity?> =
        caches.map { m ->
            m.values.firstOrNull { match(it, personaId, query, mode, regions) }
        }

    override suspend fun getCache(personaId: Long, query: String, mode: String, regions: String): SearchCacheEntity? =
        caches.value.values.firstOrNull { match(it, personaId, query, mode, regions) }

    override fun observeResultVideos(cacheId: Long): Flow<List<VideoEntity>> = combine(results, videos.rows) { r, v ->
        r.filter { it.cacheId == cacheId }.sortedBy { it.rank }.mapNotNull { v[it.videoId] }
    }

    override suspend fun resultVideoIds(cacheId: Long): List<String> = results.value.filter {
        it.cacheId == cacheId
    }.map { it.videoId }

    override suspend fun maxRank(cacheId: Long): Int =
        results.value.filter { it.cacheId == cacheId }.maxOfOrNull { it.rank } ?: -1

    override fun observeRecent(personaId: Long, mode: String, limit: Int): Flow<List<SearchCacheEntity>> =
        caches.map { m ->
            m.values.filter {
                it.personaId == personaId && it.mode == mode
            }.sortedByDescending { it.lastUsedAt }.take(limit)
        }

    override suspend fun insertCache(cache: SearchCacheEntity): Long {
        val id = nextId++
        caches.update { it + (id to cache.copy(id = id)) }
        return id
    }

    override suspend fun updateCache(cache: SearchCacheEntity) = caches.update { it + (cache.id to cache) }

    override suspend fun touch(id: Long, at: Long) =
        caches.update { m -> m[id]?.let { m + (id to it.copy(lastUsedAt = at)) } ?: m }

    override suspend fun insertResults(results: List<SearchResultEntity>) = this.results.update { it + results }

    override suspend fun deleteResults(cacheId: Long) = results.update { list ->
        list.filterNot { it.cacheId == cacheId }
    }

    override suspend fun deleteCache(id: Long) {
        caches.update { it - id }
        deleteResults(id)
    }

    override suspend fun deleteOlderThan(cutoff: Long): Int {
        val old = caches.value.values.filter { it.createdAt < cutoff }.map { it.id }
        old.forEach { deleteCache(it) }
        return old.size
    }
}
