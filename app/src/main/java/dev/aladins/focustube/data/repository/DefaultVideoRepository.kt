package dev.aladins.focustube.data.repository

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.data.mapper.toEntity
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.repository.VideoRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [VideoRepository]: saved details first; one videos.list call (1 unit) when missing. */
@Singleton
class DefaultVideoRepository @Inject constructor(
    private val dao: VideoDao,
    private val youTube: YouTubeDataSource,
    private val clock: Clock
) : VideoRepository {
    override fun observe(id: String): Flow<Video?> = dao.observe(id).map { it?.toDomain() }

    override suspend fun fetchIfMissing(id: String): AppResult<Unit> {
        if (dao.get(id) != null) return AppResult.Success(Unit)
        return when (val result = youTube.videos(listOf(id))) {
            is AppResult.Failure -> result

            is AppResult.Success -> {
                val dto = result.value.firstOrNull() ?: return AppResult.Failure(AppError.NotFound)
                dao.upsertAll(listOf(dto.toEntity(clock.now())))
                AppResult.Success(Unit)
            }
        }
    }
}
