package dev.aladins.focustube.testing

import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import dev.aladins.focustube.data.remote.youtube.dto.SearchItem
import dev.aladins.focustube.data.remote.youtube.dto.SearchItemId
import dev.aladins.focustube.data.remote.youtube.dto.SearchListResponse
import dev.aladins.focustube.data.remote.youtube.dto.Thumbnails
import dev.aladins.focustube.data.remote.youtube.dto.VideoContentDetails
import dev.aladins.focustube.data.remote.youtube.dto.VideoDto
import dev.aladins.focustube.data.remote.youtube.dto.VideoListResponse
import dev.aladins.focustube.data.remote.youtube.dto.VideoPlayer
import dev.aladins.focustube.data.remote.youtube.dto.VideoSnippet
import dev.aladins.focustube.data.remote.youtube.dto.VideoStatistics

/**
 * In-memory YouTube API for tests. Each search page returns [pageSize] ids like
 * "guitar-US-p1-0"; ids ending in a multiple of 3 are vertical 45 s Shorts. Two pages per region
 * (page 1 has a next token, page 2 doesn't). Counts calls so tests can check the units spent.
 */
class FakeYouTubeApi(private val pageSize: Int = 6) : YouTubeApi {
    var searchCalls = 0
        private set
    var videosCalls = 0
        private set

    /** Regions of each search call, in order. */
    val searchedRegions = mutableListOf<String>()

    /** When set, every call throws it (e.g. an HttpException with a quota body, or an IOException). */
    var failWith: Throwable? = null

    /** Units these calls would have cost: search.list 100, videos.list 1. */
    val unitsSpent: Long get() = searchCalls * 100L + videosCalls

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
        failWith?.let { throw it }
        searchCalls++
        searchedRegions += regionCode
        val page = if (pageToken == null) 1 else 2
        val slug = query.trim().lowercase().replace(" ", "-")
        val ids = (0 until pageSize).map { "$slug-$regionCode-p$page-$it" }
        return SearchListResponse(
            items = ids.map { SearchItem(SearchItemId(it)) },
            nextPageToken = if (page == 1) "$regionCode-token-2" else null
        )
    }

    override suspend fun videos(ids: String, part: String, maxHeight: Int): VideoListResponse {
        failWith?.let { throw it }
        videosCalls++
        return VideoListResponse(
            ids.split(",").map {
                videoDto(it, isShort = it.last().digitToIntOrNull()?.rem(3) == 0)
            }
        )
    }

    companion object {
        /** A video DTO; Shorts are 45 s and vertical, others 11:04 and wide. */
        fun videoDto(id: String, isShort: Boolean = false) = VideoDto(
            id = id,
            snippet =
                VideoSnippet(
                    title = "Video $id",
                    channelId = "channel-1",
                    channelTitle = "Fingerstyle Fred",
                    publishedAt = "2026-11-01T10:00:00Z",
                    description = "About $id",
                    thumbnails = Thumbnails()
                ),
            contentDetails = VideoContentDetails(if (isShort) "PT45S" else "PT11M4S"),
            statistics = VideoStatistics(viewCount = "84000"),
            player = if (isShort) {
                VideoPlayer(
                    embedWidth = 563,
                    embedHeight = 1000
                )
            } else {
                VideoPlayer(embedWidth = 1778, embedHeight = 1000)
            }
        )
    }
}
