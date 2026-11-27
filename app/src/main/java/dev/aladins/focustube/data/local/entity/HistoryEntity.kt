package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Row in `history`: how far a persona got in a video. One row per persona and video. */
@Entity(
    tableName = "history",
    primaryKeys = ["persona_id", "video_id"],
    foreignKeys = [ForeignKey(entity = PersonaEntity::class, parentColumns = ["id"], childColumns = ["persona_id"])],
    indices = [Index("video_id"), Index(value = ["persona_id", "last_watched_at"])]
)
data class HistoryEntity(
    @ColumnInfo(name = "persona_id") val personaId: Long,
    @ColumnInfo(name = "video_id") val videoId: String,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long,
    @ColumnInfo(name = "position_sec") val positionSec: Long,
    @ColumnInfo(name = "duration_sec") val durationSec: Long,
    @ColumnInfo(name = "is_watched") val isWatched: Boolean,
    /** When it first counted as watched; null until then. */
    @ColumnInfo(name = "watched_at") val watchedAt: Long?
)
