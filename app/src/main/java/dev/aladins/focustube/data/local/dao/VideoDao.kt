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

    /** Drops old details no saved search uses. (Library rows will keep theirs from Milestone 4.) */
    @Query(
        "DELETE FROM video WHERE fetched_at < :cutoff " +
            "AND youtube_id NOT IN (SELECT video_id FROM search_result)"
    )
    suspend fun deleteUnusedOlderThan(cutoff: Long): Int
}
