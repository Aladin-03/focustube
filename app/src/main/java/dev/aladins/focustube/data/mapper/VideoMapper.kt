package dev.aladins.focustube.data.mapper

import dev.aladins.focustube.data.local.entity.VideoEntity
import dev.aladins.focustube.data.remote.youtube.dto.VideoDto
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.model.isShort
import kotlin.time.Duration
import kotlin.time.Instant

/** YouTube's video DTO -> stored row. Duration parsing and the Shorts rule happen here, once. */
fun VideoDto.toEntity(fetchedAt: Instant): VideoEntity {
    val durationSec = parseIsoDurationSeconds(contentDetails?.duration)
    return VideoEntity(
        youtubeId = id,
        title = snippet.title,
        channelId = snippet.channelId,
        channelTitle = snippet.channelTitle,
        durationSec = durationSec,
        publishedAt = runCatching { Instant.parse(snippet.publishedAt).toEpochMilliseconds() }.getOrDefault(0),
        viewCount = statistics?.viewCount?.toLongOrNull(),
        isShort = isShort(durationSec, player?.embedWidth, player?.embedHeight),
        description = snippet.description,
        thumbnailUrl = snippet.thumbnails?.let { it.high ?: it.medium ?: it.default }?.url,
        fetchedAt = fetchedAt.toEpochMilliseconds()
    )
}

fun VideoEntity.toDomain(): Video = Video(
    id = youtubeId,
    title = title,
    channelId = channelId,
    channelTitle = channelTitle,
    durationSeconds = durationSec,
    publishedAt = Instant.fromEpochMilliseconds(publishedAt),
    viewCount = viewCount,
    isShort = isShort,
    description = description,
    thumbnailUrl = thumbnailUrl
)

/** "PT11M4S" -> 664, "P1DT2H" -> 93600; missing or unparsable (e.g. live "P0D") -> 0. */
fun parseIsoDurationSeconds(iso: String?): Long {
    if (iso.isNullOrBlank()) return 0
    return runCatching { Duration.parseIsoString(iso).inWholeSeconds }.getOrDefault(0)
}
