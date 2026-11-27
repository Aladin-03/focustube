package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.Video
import kotlinx.coroutines.flow.Flow

/** Saved video details. */
interface VideoRepository {
    fun observe(id: String): Flow<Video?>

    /** Fetches details (1 unit) only if the video isn't saved yet. */
    suspend fun fetchIfMissing(id: String): AppResult<Unit>

    /**
     * Refreshes details of Watch later / History videos fetched more than 30 days ago (YouTube API
     * policy), at most once a day: 1 unit per 50 videos. Library entries themselves are never dropped.
     */
    suspend fun refreshStaleLibraryVideos(): AppResult<Unit>
}
