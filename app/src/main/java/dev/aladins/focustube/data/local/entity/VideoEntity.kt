package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Row in `video`: every video shown anywhere. Refetched or dropped after 30 days. */
@Entity(tableName = "video", indices = [Index("fetched_at")])
data class VideoEntity(
    @PrimaryKey @ColumnInfo(name = "youtube_id") val youtubeId: String,
    val title: String,
    @ColumnInfo(name = "channel_id") val channelId: String,
    @ColumnInfo(name = "channel_title") val channelTitle: String,
    @ColumnInfo(name = "duration_sec") val durationSec: Long,
    @ColumnInfo(name = "published_at") val publishedAt: Long,
    @ColumnInfo(name = "view_count") val viewCount: Long?,
    @ColumnInfo(name = "is_short") val isShort: Boolean,
    val description: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long
)
