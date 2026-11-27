package dev.aladins.focustube.data.repository

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.data.mapper.toEntity
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.data.settings.AppPreferences
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.repository.VideoRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [VideoRepository]: saved details first; one videos.list call (1 unit) when missing. Library
 * videos older than 30 days are refreshed at most once a day.
 */
@Singleton
class DefaultVideoRepository @Inject constructor(
    private val dao: VideoDao,
    private val youTube: YouTubeDataSource,
    private val clock: Clock,
    private val preferences: AppPreferences
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

    override suspend fun refreshStaleLibraryVideos(): AppResult<Unit> {
        val now = clock.now()
        val last = preferences.libraryRefreshedAt.first()
        if (last != null &&
            now.toEpochMilliseconds() - last < 1.days.inWholeMilliseconds
        ) {
            return AppResult.Success(Unit)
        }

        val ids = dao.libraryIdsFetchedBefore((now - 30.days).toEpochMilliseconds())
        if (ids.isNotEmpty()) {
            when (val result = youTube.videos(ids)) {
                is AppResult.Failure -> return result

                // Videos YouTube no longer returns keep their old details: the entry stays in the library.
                is AppResult.Success -> dao.upsertAll(result.value.map { it.toEntity(now) })
            }
        }
        preferences.setLibraryRefreshedAt(now.toEpochMilliseconds())
        return AppResult.Success(Unit)
    }
}
