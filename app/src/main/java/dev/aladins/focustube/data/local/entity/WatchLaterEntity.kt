package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Row in `watch_later`: a persona saved a video. One row per persona and video. */
@Entity(
    tableName = "watch_later",
    primaryKeys = ["persona_id", "video_id"],
    foreignKeys = [ForeignKey(entity = PersonaEntity::class, parentColumns = ["id"], childColumns = ["persona_id"])],
    indices = [Index("video_id"), Index(value = ["persona_id", "added_at"])]
)
data class WatchLaterEntity(
    @ColumnInfo(name = "persona_id") val personaId: Long,
    @ColumnInfo(name = "video_id") val videoId: String,
    @ColumnInfo(name = "added_at") val addedAt: Long
)
