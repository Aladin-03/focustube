package dev.aladins.focustube.data.repository

import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.data.mapper.toStoredRegions
import dev.aladins.focustube.data.settings.AppPreferences
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** [PersonaRepository] backed by Room (personas) and [AppPreferences] (current persona). */
@Singleton
class DefaultPersonaRepository @Inject constructor(
    private val dao: PersonaDao,
    private val preferences: AppPreferences,
    private val clock: Clock
) : PersonaRepository {
    override fun observeActive(): Flow<List<Persona>> = dao.observeActive().map { rows -> rows.map { it.toDomain() } }

    override fun observeArchived(): Flow<List<Persona>> = dao.observeArchived().map { rows ->
        rows.map { it.toDomain() }
    }

    override fun observe(id: Long): Flow<Persona?> = dao.observe(id).map { it?.toDomain() }

    override fun observeCurrent(): Flow<Persona?> = combine(preferences.currentPersonaId, observeActive()) {
            savedId,
            active
        ->
        active.firstOrNull { it.id == savedId } ?: active.firstOrNull()
    }.distinctUntilChanged()

    override suspend fun create(name: String, goal: String?, regions: List<Region>): Long = dao.insert(
        PersonaEntity(
            name = name,
            goal = goal,
            regions = regions.toStoredRegions(),
            createdAt = clock.now().toEpochMilliseconds()
        )
    )

    override suspend fun update(id: Long, name: String, goal: String?) = dao.updateDetails(id, name, goal)

    override suspend fun setRegions(id: Long, regions: List<Region>) {
        require(regions.isNotEmpty()) { "A persona needs at least one region" }
        dao.updateRegions(id, regions.toStoredRegions())
    }

    override suspend fun archive(id: Long) = dao.setArchivedAt(id, clock.now().toEpochMilliseconds())

    override suspend fun restore(id: Long) = dao.setArchivedAt(id, null)

    override suspend fun setCurrent(id: Long) = preferences.setCurrentPersonaId(id)
}
