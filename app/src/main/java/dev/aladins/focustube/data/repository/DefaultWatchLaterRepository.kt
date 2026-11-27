package dev.aladins.focustube.data.repository

import dev.aladins.focustube.data.local.dao.WatchLaterDao
import dev.aladins.focustube.data.local.entity.WatchLaterEntity
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.domain.model.WatchLaterItem
import dev.aladins.focustube.domain.repository.WatchLaterRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [WatchLaterRepository] on Room. */
@Singleton
class DefaultWatchLaterRepository @Inject constructor(private val dao: WatchLaterDao, private val clock: Clock) :
    WatchLaterRepository {
    override fun observe(personaId: Long): Flow<List<WatchLaterItem>> =
        dao.observe(personaId).map { rows -> rows.map { it.toDomain() } }

    override fun observeIds(personaId: Long): Flow<Set<String>> = dao.observeIds(personaId).map { it.toSet() }

    override suspend fun add(personaId: Long, videoId: String) =
        dao.insert(WatchLaterEntity(personaId, videoId, clock.now().toEpochMilliseconds()))

    override suspend fun remove(personaId: Long, videoId: String) = dao.delete(personaId, videoId)

    override suspend fun restore(personaId: Long, item: WatchLaterItem) =
        dao.upsert(WatchLaterEntity(personaId, item.video.id, item.addedAt.toEpochMilliseconds()))
}
