package dev.aladins.focustube.testing

import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.model.WatchLaterItem
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.repository.HistoryRepository
import dev.aladins.focustube.domain.repository.WatchLaterRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory Watch later per persona; [videos] supplies details for ids. */
class FakeWatchLaterRepository(private val videos: Map<String, Video> = emptyMap()) : WatchLaterRepository {
    /** (personaId, videoId) -> addedAt millis. */
    val rows = MutableStateFlow<Map<Pair<Long, String>, Long>>(emptyMap())
    private var clock = 1_000L

    override fun observe(personaId: Long): Flow<List<WatchLaterItem>> = rows.map { m ->
        m.filterKeys { it.first == personaId }.entries.sortedByDescending { it.value }.map { (k, at) ->
            WatchLaterItem(videos[k.second] ?: video(k.second), Instant.fromEpochMilliseconds(at), null)
        }
    }

    override fun observeIds(personaId: Long): Flow<Set<String>> = rows.map { m ->
        m.keys.filter { it.first == personaId }.map { it.second }.toSet()
    }

    override suspend fun add(personaId: Long, videoId: String) =
        rows.update { if ((personaId to videoId) in it) it else it + ((personaId to videoId) to clock++) }

    override suspend fun remove(personaId: Long, videoId: String) = rows.update { it - (personaId to videoId) }

    override suspend fun restore(personaId: Long, item: WatchLaterItem) =
        rows.update { it + ((personaId to item.video.id) to item.addedAt.toEpochMilliseconds()) }
}

/** In-memory History per persona, with the same sticky-watched rule as the real one. */
class FakeHistoryRepository(private val videos: Map<String, Video> = emptyMap()) : HistoryRepository {
    val rows = MutableStateFlow<Map<Pair<Long, String>, Pair<WatchProgress, Instant>>>(emptyMap())
    var saveCount = 0
        private set

    override fun observe(personaId: Long): Flow<List<HistoryEntry>> = rows.map { m ->
        m.filterKeys { it.first == personaId }.entries.sortedByDescending { it.value.second }.map { (k, v) ->
            HistoryEntry(videos[k.second] ?: video(k.second), v.second, v.first)
        }
    }

    override fun observeProgress(personaId: Long): Flow<Map<String, WatchProgress>> = rows.map { m ->
        m.filterKeys {
            it.first ==
                personaId
        }.map { (k, v) -> k.second to v.first }.toMap()
    }

    override fun observeWatchedIds(personaId: Long): Flow<Set<String>> = rows.map { m ->
        m.filter {
            it.key.first ==
                personaId &&
                it.value.first.isWatched
        }.map { it.key.second }.toSet()
    }

    override suspend fun getProgress(personaId: Long, videoId: String): WatchProgress? =
        rows.value[personaId to videoId]?.first

    override suspend fun save(personaId: Long, videoId: String, progress: WatchProgress, at: Instant) {
        saveCount++
        val wasWatched = rows.value[personaId to videoId]?.first?.isWatched == true
        rows.update {
            it +
                ((personaId to videoId) to (progress.copy(isWatched = wasWatched || progress.isWatched) to at))
        }
    }

    override suspend fun remove(personaId: Long, videoId: String) = rows.update { it - (personaId to videoId) }

    override suspend fun restore(personaId: Long, entry: HistoryEntry) =
        rows.update { it + ((personaId to entry.video.id) to (entry.progress to entry.lastWatchedAt)) }

    override suspend fun clear(personaId: Long) = rows.update { m -> m.filterKeys { it.first != personaId } }
}
