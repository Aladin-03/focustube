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
     * Drops old details nothing uses: not in a saved search, not in any persona's Watch later or
     * History, and not an upload of a followed channel (archived personas included, so restoring
     * one brings everything back).
     */
    @Query(
        "DELETE FROM video WHERE fetched_at < :cutoff " +
            "AND youtube_id NOT IN (SELECT video_id FROM search_result) " +
            "AND youtube_id NOT IN (SELECT video_id FROM watch_later) " +
            "AND youtube_id NOT IN (SELECT video_id FROM history) " +
            "AND youtube_id NOT IN (SELECT cu.video_id FROM channel_upload cu JOIN persona_channel pc ON pc.channel_id = cu.channel_id)"
    )
    suspend fun deleteUnusedOlderThan(cutoff: Long): Int

    /**
     * Ids of kept videos (Watch later, History, uploads of followed channels) whose details were
     * fetched before [cutoff]: these are refreshed instead of dropped.
     */
    @Query(
        "SELECT youtube_id FROM video WHERE fetched_at < :cutoff AND (" +
            "youtube_id IN (SELECT video_id FROM watch_later) OR youtube_id IN (SELECT video_id FROM history) " +
            "OR youtube_id IN (SELECT cu.video_id FROM channel_upload cu JOIN persona_channel pc ON pc.channel_id = cu.channel_id))"
    )
    suspend fun keptIdsFetchedBefore(cutoff: Long): List<String>
}
