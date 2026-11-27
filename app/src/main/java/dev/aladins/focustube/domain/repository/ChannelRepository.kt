package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.domain.model.Video
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Channels, which personas follow them, and their uploads. Cache first: YouTube is called to find
 * a channel, to check for new uploads, or for "More uploads".
 */
interface ChannelRepository {
    /** Finds channels: a link or @handle costs 1 unit; words cost 101 (saved 30 days). */
    suspend fun find(query: ChannelQuery): AppResult<List<Channel>>

    fun observe(channelId: String): Flow<Channel?>

    /** Channels a persona follows, by name. */
    fun observeFollowed(personaId: Long): Flow<List<Channel>>

    /** Ids of personas following [channelId] (archived ones included). */
    fun observeFollowers(channelId: String): Flow<Set<Long>>

    /** For each channel, which personas follow it (the Add channel screen's "Added" state). */
    fun observeAllFollowers(): Flow<Map<String, Set<Long>>>

    /** Uploads of the persona's channels published since [since], newest first. */
    fun observeFeed(personaId: Long, since: Instant): Flow<List<Video>>

    /** Saved uploads of a channel, newest first. */
    fun observeUploads(channelId: String): Flow<List<Video>>

    /** When the persona's channels were last checked (newest check); null if never. */
    fun observeLastChecked(personaId: Long): Flow<Instant?>

    /**
     * Makes exactly [personaIds] follow [channelId]. A channel followed for the first time gets
     * its first page of uploads (2 units).
     */
    suspend fun setFollowers(channelId: String, personaIds: Set<Long>): AppResult<Unit>

    suspend fun unfollow(personaId: Long, channelId: String)

    /** Undo of [unfollow]: follows again with the original date. */
    suspend fun restoreFollow(personaId: Long, channelId: String, addedAt: Instant)

    /** When [personaId] started following [channelId], or null. */
    suspend fun followedSince(personaId: Long, channelId: String): Instant?

    /** Makes sure a channel's details and first uploads are saved (3 units if not). */
    suspend fun ensureChannel(channelId: String): AppResult<Unit>

    /** Loads the channel's next page of uploads (2 units per 50). */
    suspend fun loadMoreUploads(channelId: String): AppResult<Unit>

    /** Channels followed by [personaId], or by any active persona when null. */
    suspend fun followedIds(personaId: Long?): List<String>

    /** Those of [channelIds] not checked since [cutoff]. */
    suspend fun checkedBefore(channelIds: List<String>, cutoff: Instant): List<String>

    /**
     * Checks [channelIds] for new uploads: 1 unit per channel, plus 1 per 50 new videos (batched
     * across channels). Returns how many new uploads were found.
     */
    suspend fun checkUploads(channelIds: List<String>): AppResult<Int>

    /** Drops channels nobody follows and channel searches, when older than 30 days. */
    suspend fun removeStale()
}
