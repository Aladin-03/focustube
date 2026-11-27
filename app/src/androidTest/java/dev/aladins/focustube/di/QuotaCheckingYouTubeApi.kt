package dev.aladins.focustube.di

import dev.aladins.focustube.data.remote.quota.QuotaExceededException
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.data.remote.quota.YouTubeCosts
import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import dev.aladins.focustube.data.remote.youtube.dto.ChannelListResponse
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItemListResponse
import dev.aladins.focustube.data.remote.youtube.dto.SearchListResponse
import dev.aladins.focustube.data.remote.youtube.dto.VideoListResponse
import dev.aladins.focustube.testing.FakeYouTubeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UI tests replace the OkHttp stack (where QuotaInterceptor lives) with [FakeYouTubeApi]. This
 * wrapper applies the same rule as QuotaInterceptor, so tests see the daily limit like the app:
 * a call that would pass the limit is refused (never reaches the fake), otherwise its cost is
 * charged.
 */
class QuotaCheckingYouTubeApi(private val fake: FakeYouTubeApi, private val tracker: QuotaTracker) : YouTubeApi {
    /** Like OkHttp's interceptor thread: the blocking quota calls never run on the main thread. */
    private suspend fun spend(cost: Long) = withContext(Dispatchers.IO) {
        if (!tracker.canSpendBlocking(cost)) throw QuotaExceededException()
        tracker.chargeBlocking(cost)
    }

    override suspend fun search(
        query: String,
        regionCode: String,
        pageToken: String?,
        part: String,
        type: String,
        videoEmbeddable: String,
        safeSearch: String,
        maxResults: Int
    ): SearchListResponse {
        spend(YouTubeCosts.SEARCH)
        return fake.search(query, regionCode, pageToken, part, type, videoEmbeddable, safeSearch, maxResults)
    }

    override suspend fun videos(ids: String, part: String, maxHeight: Int): VideoListResponse {
        spend(YouTubeCosts.VIDEOS)
        return fake.videos(ids, part, maxHeight)
    }

    override suspend fun searchChannels(
        query: String,
        part: String,
        type: String,
        maxResults: Int
    ): SearchListResponse {
        spend(YouTubeCosts.SEARCH)
        return fake.searchChannels(query, part, type, maxResults)
    }

    override suspend fun channels(
        ids: String?,
        handle: String?,
        username: String?,
        part: String,
        maxResults: Int
    ): ChannelListResponse {
        spend(YouTubeCosts.VIDEOS)
        return fake.channels(ids, handle, username, part, maxResults)
    }

    override suspend fun playlistItems(
        playlistId: String,
        pageToken: String?,
        part: String,
        maxResults: Int
    ): PlaylistItemListResponse {
        spend(YouTubeCosts.VIDEOS)
        return fake.playlistItems(playlistId, pageToken, part, maxResults)
    }
}
