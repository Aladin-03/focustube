package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.aladins.focustube.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow

/** Reads and writes personas. There is deliberately no delete. */
@Dao
interface PersonaDao {
    @Query("SELECT * FROM persona WHERE archived_at IS NULL ORDER BY created_at, id")
    fun observeActive(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM persona WHERE archived_at IS NOT NULL ORDER BY archived_at DESC, id DESC")
    fun observeArchived(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM persona WHERE id = :id")
    fun observe(id: Long): Flow<PersonaEntity?>

    @Insert
    suspend fun insert(persona: PersonaEntity): Long

    @Query("UPDATE persona SET name = :name, goal = :goal WHERE id = :id")
    suspend fun updateDetails(id: Long, name: String, goal: String?)

    @Query("UPDATE persona SET regions = :regions WHERE id = :id")
    suspend fun updateRegions(id: Long, regions: String)

    /** Archives (a time) or restores (null). */
    @Query("UPDATE persona SET archived_at = :archivedAt WHERE id = :id")
    suspend fun setArchivedAt(id: Long, archivedAt: Long?)
}
