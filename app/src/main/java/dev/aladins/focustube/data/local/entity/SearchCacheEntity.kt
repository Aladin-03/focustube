package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Row in `search_cache`: one saved search. Unique per persona, normalised query, mode and
 * regions (sorted). [pageTokens] holds each region's next page token as JSON.
 */
@Entity(
    tableName = "search_cache",
    foreignKeys = [ForeignKey(entity = PersonaEntity::class, parentColumns = ["id"], childColumns = ["persona_id"])],
    indices = [
        Index(value = ["persona_id", "query_normalized", "mode", "regions"], unique = true),
        Index("created_at")
    ]
)
data class SearchCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "persona_id") val personaId: Long,
    /** As the user typed it (shown under Recent). */
    val query: String,
    @ColumnInfo(name = "query_normalized") val queryNormalized: String,
    /** "CLASSIC" or "AI". */
    val mode: String,
    /** Sorted region codes, "DE,GB,US". */
    val regions: String,
    /** Region codes in the persona's order when searched; pages take them in turn. */
    @ColumnInfo(name = "region_order") val regionOrder: String,
    @ColumnInfo(name = "pages_loaded") val pagesLoaded: Int,
    /** JSON object: region code -> next page token (absent = no more pages for it). */
    @ColumnInfo(name = "page_tokens") val pageTokens: String,
    /** When page 1 was fetched (the "saved X ago" time; refresh resets it). */
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Long
)
