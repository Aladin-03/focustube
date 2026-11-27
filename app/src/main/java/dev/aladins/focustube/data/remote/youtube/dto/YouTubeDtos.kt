package dev.aladins.focustube.data.remote.youtube.dto

import kotlinx.serialization.Serializable

/** search.list (part=id): only ids; details come from videos.list. */
@Serializable
data class SearchListResponse(val items: List<SearchItem> = emptyList(), val nextPageToken: String? = null)

@Serializable
data class SearchItem(val id: SearchItemId)

@Serializable
data class SearchItemId(val videoId: String? = null, val channelId: String? = null)

/** videos.list (part=snippet,contentDetails,statistics,player). */
@Serializable
data class VideoListResponse(val items: List<VideoDto> = emptyList())

@Serializable
data class VideoDto(
    val id: String,
    val snippet: VideoSnippet,
    val contentDetails: VideoContentDetails? = null,
    val statistics: VideoStatistics? = null,
    val player: VideoPlayer? = null
)

@Serializable
data class VideoSnippet(
    val title: String,
    val channelId: String,
    val channelTitle: String = "",
    val publishedAt: String,
    val description: String = "",
    val thumbnails: Thumbnails? = null
)

@Serializable
data class Thumbnails(
    val medium: ThumbnailDto? = null,
    val high: ThumbnailDto? = null,
    val default: ThumbnailDto? = null
)

@Serializable
data class ThumbnailDto(val url: String)

@Serializable
data class VideoContentDetails(
    /** ISO 8601, e.g. "PT11M4S"; "P0D" for live. */
    val duration: String? = null
)

@Serializable
data class VideoStatistics(
    /** YouTube sends counts as strings; hidden counts are missing. */
    val viewCount: String? = null
)

/** Embed size, present because we pass maxHeight. Taller than wide = vertical. */
@Serializable
data class VideoPlayer(val embedWidth: Long? = null, val embedHeight: Long? = null)

/** Error body: {"error": {"code": 403, "errors": [{"reason": "quotaExceeded"}]}}. */
@Serializable
data class YouTubeErrorResponse(val error: YouTubeErrorBody)

@Serializable
data class YouTubeErrorBody(
    val code: Int = 0,
    val message: String = "",
    val errors: List<YouTubeErrorItem> = emptyList(),
    val details: List<YouTubeErrorDetail> = emptyList()
)

@Serializable
data class YouTubeErrorItem(val reason: String = "")

@Serializable
data class YouTubeErrorDetail(val reason: String = "")

/** channels.list (part=snippet,statistics,contentDetails). */
@Serializable
data class ChannelListResponse(val items: List<ChannelDto> = emptyList())

@Serializable
data class ChannelDto(
    val id: String,
    val snippet: ChannelSnippet,
    val statistics: ChannelStatistics? = null,
    val contentDetails: ChannelContentDetails? = null
)

@Serializable
data class ChannelSnippet(
    val title: String,
    /** "@fingerstylefred". */
    val customUrl: String? = null,
    val thumbnails: Thumbnails? = null
)

@Serializable
data class ChannelStatistics(
    val subscriberCount: String? = null,
    val hiddenSubscriberCount: Boolean = false,
    val videoCount: String? = null
)

@Serializable
data class ChannelContentDetails(val relatedPlaylists: RelatedPlaylists? = null)

@Serializable
data class RelatedPlaylists(
    /** The channel's uploads playlist ("UU…"). */
    val uploads: String? = null
)

/** playlistItems.list (part=contentDetails) on an uploads playlist: newest first. */
@Serializable
data class PlaylistItemListResponse(val items: List<PlaylistItem> = emptyList(), val nextPageToken: String? = null)

@Serializable
data class PlaylistItem(val contentDetails: PlaylistItemDetails)

@Serializable
data class PlaylistItemDetails(val videoId: String, val videoPublishedAt: String? = null)
