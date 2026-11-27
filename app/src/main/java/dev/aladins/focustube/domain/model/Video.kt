package dev.aladins.focustube.domain.model

import kotlin.time.Instant

/** A YouTube video as the app shows it. */
data class Video(
    val id: String,
    val title: String,
    val channelId: String,
    val channelTitle: String,
    val durationSeconds: Long,
    val publishedAt: Instant,
    val viewCount: Long?,
    val isShort: Boolean,
    val description: String,
    val thumbnailUrl: String?
)

/**
 * A Short is 3 minutes or less and vertical (YouTube allows Shorts up to 3 minutes). Live or
 * upcoming videos (duration 0) are never Shorts. Vertical comes from the player size YouTube
 * reports; without it the video is treated as a normal video.
 */
fun isShort(durationSeconds: Long, playerWidth: Long?, playerHeight: Long?): Boolean {
    if (durationSeconds !in 1..SHORT_MAX_SECONDS) return false
    if (playerWidth == null || playerHeight == null) return false
    return playerHeight > playerWidth
}

/** Longest a Short can be. */
const val SHORT_MAX_SECONDS = 180L
