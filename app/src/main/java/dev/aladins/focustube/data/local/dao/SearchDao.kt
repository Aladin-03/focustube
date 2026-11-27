package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.aladins.focustube.data.local.entity.SearchCacheEntity
import dev.aladins.focustube.data.local.entity.SearchResultEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

/** Saved searches and their results. */
@Dao
interface SearchDao {
    @Query(
        "SELECT * FROM search_cache WHERE persona_id = :personaId AND query_normalized = :query " +
            "AND mode = :mode AND regions = :regions"
    )
    fun observeCache(personaId: Long, query: String, mode: String, regions: String): Flow<SearchCacheEntity?>

    @Query(
        "SELECT * FROM search_cache WHERE persona_id = :personaId AND query_normalized = :query " +
            "AND mode = :mode AND regions = :regions"
    )
    suspend fun getCache(personaId: Long, query: String, mode: String, regions: String): SearchCacheEntity?

    /** The saved videos of a search, in result order. */
    @Query(
        "SELECT video.* FROM search_result JOIN video ON video.youtube_id = search_result.video_id " +
            "WHERE search_result.cache_id = :cacheId ORDER BY search_result.rank"
    )
    fun observeResultVideos(cacheId: Long): Flow<List<VideoEntity>>

    @Query("SELECT video_id FROM search_result WHERE cache_id = :cacheId")
    suspend fun resultVideoIds(cacheId: Long): List<String>

    @Query("SELECT COALESCE(MAX(rank), -1) FROM search_result WHERE cache_id = :cacheId")
    suspend fun maxRank(cacheId: Long): Int

    @Query(
        "SELECT * FROM search_cache WHERE persona_id = :personaId AND mode = :mode ORDER BY last_used_at DESC LIMIT :limit"
    )
    fun observeRecent(personaId: Long, mode: String, limit: Int): Flow<List<SearchCacheEntity>>

    @Insert
    suspend fun insertCache(cache: SearchCacheEntity): Long

    @Update
    suspend fun updateCache(cache: SearchCacheEntity)

    @Query("UPDATE search_cache SET last_used_at = :at WHERE id = :id")
    suspend fun touch(id: Long, at: Long)

    @Insert
    suspend fun insertResults(results: List<SearchResultEntity>)

    @Query("DELETE FROM search_result WHERE cache_id = :cacheId")
    suspend fun deleteResults(cacheId: Long)

    @Query("DELETE FROM search_cache WHERE id = :id")
    suspend fun deleteCache(id: Long)

    @Query("DELETE FROM search_cache WHERE created_at < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int

    /** Replaces a search's results (Refresh or a new page 1). */
    @Transaction
    suspend fun replaceResults(cacheId: Long, results: List<SearchResultEntity>) {
        deleteResults(cacheId)
        insertResults(results)
    }
}
