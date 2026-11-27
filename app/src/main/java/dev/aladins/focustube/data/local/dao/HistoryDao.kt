package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.aladins.focustube.data.local.entity.HistoryEntity
import dev.aladins.focustube.data.local.entity.HistoryRow
import kotlinx.coroutines.flow.Flow

/** A persona's History. */
@Dao
interface HistoryDao {
    @Query(
        "SELECT video.*, h.last_watched_at, h.position_sec, h.duration_sec AS history_duration_sec, " +
            "h.is_watched FROM history h " +
            "JOIN video ON video.youtube_id = h.video_id WHERE h.persona_id = :personaId " +
            "ORDER BY h.last_watched_at DESC"
    )
    fun observe(personaId: Long): Flow<List<HistoryRow>>

    @Query("SELECT * FROM history WHERE persona_id = :personaId")
    fun observeEntries(personaId: Long): Flow<List<HistoryEntity>>

    @Query("SELECT video_id FROM history WHERE persona_id = :personaId AND is_watched = 1")
    fun observeWatchedIds(personaId: Long): Flow<List<String>>

    @Query("SELECT * FROM history WHERE persona_id = :personaId AND video_id = :videoId")
    suspend fun get(personaId: Long, videoId: String): HistoryEntity?

    @Upsert
    suspend fun upsert(entry: HistoryEntity)

    @Query("DELETE FROM history WHERE persona_id = :personaId AND video_id = :videoId")
    suspend fun delete(personaId: Long, videoId: String)

    @Query("DELETE FROM history WHERE persona_id = :personaId")
    suspend fun clear(personaId: Long)
}
