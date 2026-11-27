package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Row in `channel`: every channel the app has seen. Followed ones are never deleted. */
@Entity(tableName = "channel", indices = [Index("fetched_at")])
data class ChannelEntity(
    @PrimaryKey @ColumnInfo(name = "youtube_id") val youtubeId: String,
    val title: String,
    val handle: String?,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String?,
    @ColumnInfo(name = "subscriber_count") val subscriberCount: Long?,
    @ColumnInfo(name = "video_count") val videoCount: Long?,
    @ColumnInfo(name = "uploads_playlist_id") val uploadsPlaylistId: String,
    /** Next page of the uploads playlist after what is saved; null = no more (or not loaded). */
    @ColumnInfo(name = "uploads_next_token") val uploadsNextToken: String? = null,
    @ColumnInfo(name = "last_checked_at") val lastCheckedAt: Long? = null,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long
)

/** Row in `persona_channel`: a persona follows a channel. A channel can have several. */
@Entity(
    tableName = "persona_channel",
    primaryKeys = ["persona_id", "channel_id"],
    foreignKeys = [
        ForeignKey(entity = PersonaEntity::class, parentColumns = ["id"], childColumns = ["persona_id"]),
        ForeignKey(entity = ChannelEntity::class, parentColumns = ["youtube_id"], childColumns = ["channel_id"])
    ],
    indices = [Index("channel_id")]
)
data class PersonaChannelEntity(
    @ColumnInfo(name = "persona_id") val personaId: Long,
    @ColumnInfo(name = "channel_id") val channelId: String,
    @ColumnInfo(name = "added_at") val addedAt: Long
)

/** Row in `channel_upload`: a video in a channel's uploads (details are in `video`). */
@Entity(
    tableName = "channel_upload",
    primaryKeys = ["channel_id", "video_id"],
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["youtube_id"],
            childColumns = ["channel_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["channel_id", "published_at"]), Index("video_id")]
)
data class ChannelUploadEntity(
    @ColumnInfo(name = "channel_id") val channelId: String,
    @ColumnInfo(name = "video_id") val videoId: String,
    @ColumnInfo(name = "published_at") val publishedAt: Long
)

/** Row in `channel_search`: a saved channel name search (reused for 30 days). */
@Entity(tableName = "channel_search")
data class ChannelSearchEntity(
    @PrimaryKey @ColumnInfo(name = "query_normalized") val queryNormalized: String,
    /** Channel ids in YouTube's order, comma-separated. */
    @ColumnInfo(name = "channel_ids") val channelIds: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

/** A channel with its newest saved upload time. */
data class ChannelRow(
    @Embedded val channel: ChannelEntity,
    @ColumnInfo(name = "latest_upload_at") val latestUploadAt: Long?
)
