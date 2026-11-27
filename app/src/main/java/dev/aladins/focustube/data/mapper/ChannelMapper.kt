package dev.aladins.focustube.data.mapper

import dev.aladins.focustube.data.local.entity.ChannelEntity
import dev.aladins.focustube.data.local.entity.ChannelRow
import dev.aladins.focustube.data.remote.youtube.dto.ChannelDto
import dev.aladins.focustube.domain.model.Channel
import kotlin.time.Instant

/** YouTube's channel DTO -> stored row; null when it has no uploads playlist (can't be followed). */
fun ChannelDto.toEntity(fetchedAt: Instant): ChannelEntity? {
    val uploads = contentDetails?.relatedPlaylists?.uploads ?: return null
    return ChannelEntity(
        youtubeId = id,
        title = snippet.title,
        handle = snippet.customUrl?.takeIf { it.startsWith("@") },
        avatarUrl = snippet.thumbnails?.let { it.medium ?: it.high ?: it.default }?.url,
        subscriberCount = statistics?.takeUnless { it.hiddenSubscriberCount }?.subscriberCount?.toLongOrNull(),
        videoCount = statistics?.videoCount?.toLongOrNull(),
        uploadsPlaylistId = uploads,
        fetchedAt = fetchedAt.toEpochMilliseconds()
    )
}

fun ChannelEntity.toDomain(latestUploadAt: Long? = null): Channel = Channel(
    id = youtubeId,
    title = title,
    handle = handle,
    avatarUrl = avatarUrl,
    subscriberCount = subscriberCount,
    videoCount = videoCount,
    lastCheckedAt = lastCheckedAt?.let(Instant::fromEpochMilliseconds),
    latestUploadAt = latestUploadAt?.let(Instant::fromEpochMilliseconds),
    hasMoreUploads = uploadsNextToken != null
)

fun ChannelRow.toDomain(): Channel = channel.toDomain(latestUploadAt)
