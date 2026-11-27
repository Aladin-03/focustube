package dev.aladins.focustube.testing

import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import dev.aladins.focustube.data.remote.youtube.dto.ChannelContentDetails
import dev.aladins.focustube.data.remote.youtube.dto.ChannelDto
import dev.aladins.focustube.data.remote.youtube.dto.ChannelListResponse
import dev.aladins.focustube.data.remote.youtube.dto.ChannelSnippet
import dev.aladins.focustube.data.remote.youtube.dto.ChannelStatistics
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItem
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItemDetails
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItemListResponse
import dev.aladins.focustube.data.remote.youtube.dto.RelatedPlaylists
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * In-memory YouTube API for tests. Counts calls so tests can check the units spent.
 *
 * Videos: each search page returns [pageSize] ids like "guitar-US-p1-0"; ids ending in a multiple
 * of 3 are vertical 45 s Shorts. Two pages per region (page 1 has a next token, page 2 doesn't).
 *
 * Channels: any handle or name works. "@fred" (or a search for "fred") gives channel "UC_fred"
 * with uploads playlist "UU_fred": two pages of 6 uploads "fred-u0".."fred-u11", upload uN
 * published N days and 1 hour before [clock]'s now. [addUpload] puts a new one (published now) on top.
 */
class FakeYouTubeApi(private val pageSize: Int = 6, private val clock: Clock = Clock.System) : YouTubeApi {
    var searchCalls = 0
        private set
    var videosCalls = 0
        private set
    var channelSearchCalls = 0
        private set
    var channelsCalls = 0
        private set
    var playlistCalls = 0
        private set

    /** Regions of each search call, in order. */
    val searchedRegions = mutableListOf<String>()

    /** When set, every call throws it (e.g. an HttpException with a quota body, or an IOException). */
    var failWith: Throwable? = null

    /** Newest-first extra uploads per channel slug (see [addUpload]). */
    private val extraUploads = mutableMapOf<String, MutableList<String>>()

    /** When each [addUpload] video was published. */
    private val extraPublished = mutableMapOf<String, Instant>()

    /** Units these calls would have cost: search.list 100; videos, channels, playlistItems 1. */
    val unitsSpent: Long get() = (searchCalls + channelSearchCalls) * 100L + videosCalls + channelsCalls + playlistCalls

    /** A new upload (published now) appears at the top of [slug]'s uploads. */
    fun addUpload(slug: String, videoId: String) {
        extraUploads.getOrPut(slug) { mutableListOf() }.add(0, videoId)
        extraPublished[videoId] = clock.now()
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
        failWith?.let { throw it }
        searchCalls++
        searchedRegions += regionCode
        val page = if (pageToken == null) 1 else 2
        val slug = query.trim().lowercase().replace(" ", "-")
        val ids = (0 until pageSize).map { "$slug-$regionCode-p$page-$it" }
        return SearchListResponse(
            items = ids.map { SearchItem(SearchItemId(videoId = it)) },
            nextPageToken = if (page == 1) "$regionCode-token-2" else null
        )
    }

    override suspend fun videos(ids: String, part: String, maxHeight: Int): VideoListResponse {
        failWith?.let { throw it }
        videosCalls++
        return VideoListResponse(
            ids.split(",").map { id ->
                val uploadAge = UPLOAD_ID.find(id)?.groupValues?.get(1)?.toIntOrNull()
                val published = extraPublished[id] ?: uploadAge?.let { clock.now() - it.days - 1.hours }
                videoDto(id, isShort = id.last().digitToIntOrNull()?.rem(3) == 0, publishedAt = published)
            }
        )
    }

    override suspend fun searchChannels(
        query: String,
        part: String,
        type: String,
        maxResults: Int
    ): SearchListResponse {
        failWith?.let { throw it }
        channelSearchCalls++
        val slug = query.trim().lowercase().replace(" ", "")
        return SearchListResponse(
            items = listOf(slug, "${slug}tv").map {
                SearchItem(SearchItemId(channelId = "UC_$it"))
            }
        )
    }

    override suspend fun channels(
        ids: String?,
        handle: String?,
        username: String?,
        part: String,
        maxResults: Int
    ): ChannelListResponse {
        failWith?.let { throw it }
        channelsCalls++
        // YouTube answers with the ids it was asked for; handles and usernames map to "UC_<slug>".
        val dtos =
            when {
                ids != null -> ids.split(",").map { id -> channelDto(id.removePrefix("UC_"), id) }
                handle != null -> listOf(channelDto(handle.removePrefix("@").lowercase()))
                username != null -> listOf(channelDto(username.lowercase()))
                else -> emptyList()
            }
        return ChannelListResponse(dtos)
    }

    override suspend fun playlistItems(
        playlistId: String,
        pageToken: String?,
        part: String,
        maxResults: Int
    ): PlaylistItemListResponse {
        failWith?.let { throw it }
        playlistCalls++
        val slug = playlistId.removePrefix("UU_")
        val now = clock.now()
        val page = if (pageToken == null) 0..5 else 6..11
        val base = page.map { n -> "$slug-u$n" to (now - n.days - 1.hours) }
        val extra = if (pageToken == null) extraUploads[slug].orEmpty().map { it to now } else emptyList()
        return PlaylistItemListResponse(
            items = (extra + base).map { (id, at) -> PlaylistItem(PlaylistItemDetails(id, at.toString())) },
            nextPageToken = if (pageToken == null) "$slug-page-2" else null
        )
    }

    companion object {
        private val UPLOAD_ID = Regex("-u(\\d+)$")

        /** A video DTO; Shorts are 45 s and vertical, others 11:04 and wide. */
        fun videoDto(id: String, isShort: Boolean = false, publishedAt: Instant? = null) = VideoDto(
            id = id,
            snippet =
                VideoSnippet(
                    title = "Video $id",
                    channelId = "channel-1",
                    channelTitle = "Fingerstyle Fred",
                    publishedAt = publishedAt?.toString() ?: "2026-11-01T10:00:00Z",
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

        /** A test channel "UC_<slug>" with uploads playlist "UU_<slug>". */
        fun channelDto(slug: String, id: String = "UC_$slug") = ChannelDto(
            id = id,
            snippet =
                ChannelSnippet(
                    title = slug.replaceFirstChar { it.uppercase() } + " Channel",
                    customUrl = "@$slug",
                    thumbnails = Thumbnails()
                ),
            statistics = ChannelStatistics(subscriberCount = "48200", videoCount = "312"),
            contentDetails = ChannelContentDetails(RelatedPlaylists(uploads = "UU_$slug"))
        )
    }
}
