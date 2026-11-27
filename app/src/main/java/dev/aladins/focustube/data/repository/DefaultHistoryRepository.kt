package dev.aladins.focustube.data.repository

import dev.aladins.focustube.data.local.dao.HistoryDao
import dev.aladins.focustube.data.local.entity.HistoryEntity
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.data.mapper.toProgress
import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [HistoryRepository] on Room. Watched is sticky; the first watched time is kept. */
@Singleton
class DefaultHistoryRepository @Inject constructor(private val dao: HistoryDao) : HistoryRepository {
    override fun observe(personaId: Long): Flow<List<HistoryEntry>> =
        dao.observe(personaId).map { rows -> rows.map { it.toDomain() } }

    override fun observeProgress(personaId: Long): Flow<Map<String, WatchProgress>> =
        dao.observeEntries(personaId).map { rows ->
            rows.associate {
                it.videoId to
                    it.toProgress()
            }
        }

    override fun observeWatchedIds(personaId: Long): Flow<Set<String>> = dao.observeWatchedIds(personaId).map {
        it.toSet()
    }

    override suspend fun getProgress(personaId: Long, videoId: String): WatchProgress? =
        dao.get(personaId, videoId)?.toProgress()

    override suspend fun save(personaId: Long, videoId: String, progress: WatchProgress, at: Instant) {
        val before = dao.get(personaId, videoId)
        val watched = before?.isWatched == true || progress.isWatched
        dao.upsert(
            HistoryEntity(
                personaId = personaId,
                videoId = videoId,
                lastWatchedAt = at.toEpochMilliseconds(),
                positionSec = progress.positionSeconds,
                durationSec = progress.durationSeconds,
                isWatched = watched,
                watchedAt = before?.watchedAt ?: if (watched) at.toEpochMilliseconds() else null
            )
        )
    }

    override suspend fun remove(personaId: Long, videoId: String) = dao.delete(personaId, videoId)

    override suspend fun restore(personaId: Long, entry: HistoryEntry) = dao.upsert(
        HistoryEntity(
            personaId = personaId,
            videoId = entry.video.id,
            lastWatchedAt = entry.lastWatchedAt.toEpochMilliseconds(),
            positionSec = entry.progress.positionSeconds,
            durationSec = entry.progress.durationSeconds,
            isWatched = entry.progress.isWatched,
            watchedAt = if (entry.progress.isWatched) entry.lastWatchedAt.toEpochMilliseconds() else null
        )
    )

    override suspend fun clear(personaId: Long) = dao.clear(personaId)
}
