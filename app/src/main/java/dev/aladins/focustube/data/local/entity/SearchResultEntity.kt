package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Row in `search_result`: a video at a position in a saved search. */
@Entity(
    tableName = "search_result",
    primaryKeys = ["cache_id", "rank"],
    foreignKeys = [
        ForeignKey(
            entity = SearchCacheEntity::class,
            parentColumns = ["id"],
            childColumns = ["cache_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("video_id")]
)
data class SearchResultEntity(
    @ColumnInfo(name = "cache_id") val cacheId: Long,
    @ColumnInfo(name = "video_id") val videoId: String,
    val rank: Int,
    /** AI search only (Milestone 7). */
    val reason: String? = null,
    /** AI search only (Milestone 7). */
    val level: String? = null
)
