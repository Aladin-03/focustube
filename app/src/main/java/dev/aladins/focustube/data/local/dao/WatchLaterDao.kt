package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import dev.aladins.focustube.data.local.entity.WatchLaterEntity
import dev.aladins.focustube.data.local.entity.WatchLaterRow
import kotlinx.coroutines.flow.Flow

/** A persona's Watch later list. */
@Dao
interface WatchLaterDao {
    @Query(
        "SELECT video.*, w.added_at, h.position_sec, h.duration_sec AS history_duration_sec, h.is_watched " +
            "FROM watch_later w " +
            "JOIN video ON video.youtube_id = w.video_id " +
            "LEFT JOIN history h ON h.persona_id = w.persona_id AND h.video_id = w.video_id " +
            "WHERE w.persona_id = :personaId ORDER BY w.added_at DESC"
    )
    fun observe(personaId: Long): Flow<List<WatchLaterRow>>

    @Query("SELECT video_id FROM watch_later WHERE persona_id = :personaId")
    fun observeIds(personaId: Long): Flow<List<String>>

    /** Saving an already saved video keeps its original date. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: WatchLaterEntity)

    /** Undo: puts the row back exactly as it was. */
    @Upsert
    suspend fun upsert(item: WatchLaterEntity)

    @Query("DELETE FROM watch_later WHERE persona_id = :personaId AND video_id = :videoId")
    suspend fun delete(personaId: Long, videoId: String)
}
