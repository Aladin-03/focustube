package dev.aladins.focustube.data.remote.youtube

import dev.aladins.focustube.data.remote.youtube.dto.ChannelListResponse
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItemListResponse
import dev.aladins.focustube.data.remote.youtube.dto.SearchListResponse
import dev.aladins.focustube.data.remote.youtube.dto.VideoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * YouTube Data API v3. The key is added by [YouTubeKeyInterceptor]; costs are charged by the
 * quota interceptor (search.list 100 units; videos.list, channels.list and playlistItems.list
 * 1 unit per call of up to 50 items).
 */
interface YouTubeApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("regionCode") regionCode: String,
        @Query("pageToken") pageToken: String? = null,
        @Query("part") part: String = "id",
        @Query("type") type: String = "video",
        @Query("videoEmbeddable") videoEmbeddable: String = "true",
        @Query("safeSearch") safeSearch: String = "moderate",
        @Query("maxResults") maxResults: Int = MAX_RESULTS
    ): SearchListResponse

    @GET("videos")
    suspend fun videos(
        /** Up to 50 ids, comma-separated. */
        @Query("id") ids: String,
        @Query("part") part: String = "snippet,contentDetails,statistics,player",
        /** Makes YouTube report the embed size, which tells vertical (Shorts) from wide. */
        @Query("maxHeight") maxHeight: Int = 1000
    ): VideoListResponse

    /** Channels whose names match [query] (100 units). */
    @GET("search")
    suspend fun searchChannels(
        @Query("q") query: String,
        @Query("part") part: String = "id",
        @Query("type") type: String = "channel",
        @Query("maxResults") maxResults: Int = 20
    ): SearchListResponse

    /** Channel details by ids (up to 50), by handle, or by old username: 1 unit. Give exactly one. */
    @GET("channels")
    suspend fun channels(
        @Query("id") ids: String? = null,
        @Query("forHandle") handle: String? = null,
        @Query("forUsername") username: String? = null,
        @Query("part") part: String = "snippet,statistics,contentDetails",
        @Query("maxResults") maxResults: Int = MAX_RESULTS
    ): ChannelListResponse

    /** A page of an uploads playlist, newest first (1 unit). */
    @GET("playlistItems")
    suspend fun playlistItems(
        @Query("playlistId") playlistId: String,
        @Query("pageToken") pageToken: String? = null,
        @Query("part") part: String = "contentDetails",
        @Query("maxResults") maxResults: Int = MAX_RESULTS
    ): PlaylistItemListResponse

    companion object {
        const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
        const val HOST = "www.googleapis.com"
        const val MAX_RESULTS = 50
    }
}
