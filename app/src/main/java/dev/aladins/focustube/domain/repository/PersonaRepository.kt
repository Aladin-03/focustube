package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import kotlinx.coroutines.flow.Flow

/** Personas and which one is current. Nothing here ever deletes a persona. */
interface PersonaRepository {
    /** Active personas, in creation order. */
    fun observeActive(): Flow<List<Persona>>

    /** Archived personas, most recently archived first. */
    fun observeArchived(): Flow<List<Persona>>

    /** One persona, active or archived; null if there is no such id. */
    fun observe(id: Long): Flow<Persona?>

    /**
     * The current persona. If the saved choice is missing or archived, this is the oldest active
     * persona. Null only when there are no active personas (first launch).
     */
    fun observeCurrent(): Flow<Persona?>

    /** Saves a new active persona and returns its id. Inputs must already be validated. */
    suspend fun create(name: String, goal: String?, regions: List<Region>): Long

    /** Changes name and goal. Inputs must already be validated. */
    suspend fun update(id: Long, name: String, goal: String?)

    suspend fun archive(id: Long)

    suspend fun restore(id: Long)

    /** Remembers [id] as the current persona across restarts. */
    suspend fun setCurrent(id: Long)
}
