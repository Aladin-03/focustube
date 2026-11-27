package dev.aladins.focustube.data.remote.youtube

import dev.aladins.focustube.data.remote.youtube.dto.SearchListResponse
import dev.aladins.focustube.data.remote.youtube.dto.VideoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * YouTube Data API v3. The key is added by [YouTubeKeyInterceptor]; costs are charged by the
 * quota interceptor (search.list 100 units, videos.list 1 unit for up to 50 ids).
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

    companion object {
        const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
        const val HOST = "www.googleapis.com"
        const val MAX_RESULTS = 50
    }
}
