package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.aladins.focustube.data.local.entity.ChannelEntity
import dev.aladins.focustube.data.local.entity.ChannelRow
import dev.aladins.focustube.data.local.entity.ChannelSearchEntity
import dev.aladins.focustube.data.local.entity.ChannelUploadEntity
import dev.aladins.focustube.data.local.entity.PersonaChannelEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

/** Channels, who follows them, their uploads, and saved channel searches. */
@Dao
interface ChannelDao {
    @Query(
        "SELECT channel.*, (SELECT MAX(published_at) FROM channel_upload WHERE channel_id = channel.youtube_id) " +
            "AS latest_upload_at FROM channel WHERE youtube_id = :id"
    )
    fun observe(id: String): Flow<ChannelRow?>

    @Query("SELECT * FROM channel WHERE youtube_id = :id")
    suspend fun get(id: String): ChannelEntity?

    @Query("SELECT * FROM channel WHERE youtube_id IN (:ids)")
    suspend fun getAll(ids: List<String>): List<ChannelEntity>

    @Query(
        "SELECT channel.*, (SELECT MAX(published_at) FROM channel_upload WHERE channel_id = channel.youtube_id) " +
            "AS latest_upload_at FROM persona_channel pc JOIN channel ON channel.youtube_id = pc.channel_id " +
            "WHERE pc.persona_id = :personaId ORDER BY channel.title COLLATE NOCASE"
    )
    fun observeFollowed(personaId: Long): Flow<List<ChannelRow>>

    @Query("SELECT persona_id FROM persona_channel WHERE channel_id = :channelId")
    fun observeFollowers(channelId: String): Flow<List<Long>>

    @Query("SELECT * FROM persona_channel")
    fun observeAllFollows(): Flow<List<PersonaChannelEntity>>

    @Query("SELECT * FROM persona_channel WHERE persona_id = :personaId AND channel_id = :channelId")
    suspend fun getFollow(personaId: Long, channelId: String): PersonaChannelEntity?

    @Query("SELECT persona_id FROM persona_channel WHERE channel_id = :channelId")
    suspend fun followers(channelId: String): List<Long>

    @Query(
        "SELECT video.* FROM persona_channel pc JOIN channel_upload cu ON cu.channel_id = pc.channel_id " +
            "JOIN video ON video.youtube_id = cu.video_id " +
            "WHERE pc.persona_id = :personaId AND cu.published_at >= :since " +
            "ORDER BY cu.published_at DESC"
    )
    fun observeFeed(personaId: Long, since: Long): Flow<List<VideoEntity>>

    @Query(
        "SELECT video.* FROM channel_upload cu JOIN video ON video.youtube_id = cu.video_id " +
            "WHERE cu.channel_id = :channelId ORDER BY cu.published_at DESC"
    )
    fun observeUploads(channelId: String): Flow<List<VideoEntity>>

    @Query(
        "SELECT MAX(channel.last_checked_at) FROM persona_channel pc " +
            "JOIN channel ON channel.youtube_id = pc.channel_id " +
            "WHERE pc.persona_id = :personaId"
    )
    fun observeLastChecked(personaId: Long): Flow<Long?>

    @Query("SELECT channel_id FROM persona_channel WHERE persona_id = :personaId")
    suspend fun followedIds(personaId: Long): List<String>

    /** Channels followed by at least one active (not archived) persona. */
    @Query(
        "SELECT DISTINCT pc.channel_id FROM persona_channel pc JOIN persona p ON p.id = pc.persona_id " +
            "WHERE p.archived_at IS NULL"
    )
    suspend fun followedIdsOfActivePersonas(): List<String>

    @Query("SELECT video_id FROM channel_upload WHERE channel_id = :channelId")
    suspend fun uploadIds(channelId: String): List<String>

    /** Saves new channels; for known ones only the details change (paging and check times stay). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(channel: ChannelEntity)

    @Query(
        "UPDATE channel SET title = :title, handle = :handle, avatar_url = :avatarUrl, " +
            "subscriber_count = :subscriberCount, video_count = :videoCount, uploads_playlist_id = :uploads, " +
            "fetched_at = :fetchedAt WHERE youtube_id = :id"
    )
    suspend fun updateDetails(
        id: String,
        title: String,
        handle: String?,
        avatarUrl: String?,
        subscriberCount: Long?,
        videoCount: Long?,
        uploads: String,
        fetchedAt: Long
    )

    @Transaction
    suspend fun upsertDetails(channels: List<ChannelEntity>) {
        channels.forEach { c ->
            insertIgnore(c)
            updateDetails(
                c.youtubeId,
                c.title,
                c.handle,
                c.avatarUrl,
                c.subscriberCount,
                c.videoCount,
                c.uploadsPlaylistId,
                c.fetchedAt
            )
        }
    }

    @Query("UPDATE channel SET uploads_next_token = :token WHERE youtube_id = :id")
    suspend fun setNextToken(id: String, token: String?)

    @Query("UPDATE channel SET last_checked_at = :at WHERE youtube_id IN (:ids)")
    suspend fun setLastChecked(ids: List<String>, at: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUploads(uploads: List<ChannelUploadEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun follow(follow: PersonaChannelEntity)

    @Upsert
    suspend fun upsertFollow(follow: PersonaChannelEntity)

    @Query("DELETE FROM persona_channel WHERE persona_id = :personaId AND channel_id = :channelId")
    suspend fun unfollow(personaId: Long, channelId: String)

    @Query("SELECT * FROM channel_search WHERE query_normalized = :query")
    suspend fun getSearch(query: String): ChannelSearchEntity?

    @Upsert
    suspend fun saveSearch(search: ChannelSearchEntity)

    /** Channels nobody follows, fetched before [cutoff] (their uploads go with them). */
    @Query(
        "DELETE FROM channel WHERE fetched_at < :cutoff AND youtube_id NOT IN (SELECT channel_id FROM persona_channel)"
    )
    suspend fun deleteUnfollowedOlderThan(cutoff: Long): Int

    @Query("DELETE FROM channel_search WHERE created_at < :cutoff")
    suspend fun deleteSearchesOlderThan(cutoff: Long): Int
}
