package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.aladins.focustube.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

/** Video details. */
@Dao
interface VideoDao {
    @Upsert
    suspend fun upsertAll(videos: List<VideoEntity>)

    @Query("SELECT * FROM video WHERE youtube_id = :id")
    fun observe(id: String): Flow<VideoEntity?>

    @Query("SELECT * FROM video WHERE youtube_id = :id")
    suspend fun get(id: String): VideoEntity?

    /**
     * Drops old details nothing uses: not in a saved search, nor in any persona's Watch later or
     * History (archived personas included, so restoring one brings everything back).
     */
    @Query(
        "DELETE FROM video WHERE fetched_at < :cutoff " +
            "AND youtube_id NOT IN (SELECT video_id FROM search_result) " +
            "AND youtube_id NOT IN (SELECT video_id FROM watch_later) " +
            "AND youtube_id NOT IN (SELECT video_id FROM history)"
    )
    suspend fun deleteUnusedOlderThan(cutoff: Long): Int

    /** Ids of Watch later / History videos whose details were fetched before [cutoff]. */
    @Query(
        "SELECT youtube_id FROM video WHERE fetched_at < :cutoff " +
            "AND (youtube_id IN (SELECT video_id FROM watch_later) OR youtube_id IN (SELECT video_id FROM history))"
    )
    suspend fun libraryIdsFetchedBefore(cutoff: Long): List<String>
}
