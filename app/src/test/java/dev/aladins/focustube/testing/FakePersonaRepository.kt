package dev.aladins.focustube.testing

import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.PersonaRepository
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [PersonaRepository] with the same ordering and current-persona rules as the real one. */
class FakePersonaRepository(initial: List<Persona> = emptyList(), currentId: Long? = null) : PersonaRepository {
    private val personas = MutableStateFlow(initial)
    private val currentIdFlow = MutableStateFlow(currentId)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1
    private var tick = Instant.fromEpochMilliseconds(1_000_000)

    val all: List<Persona> get() = personas.value
    val savedCurrentId: Long? get() = currentIdFlow.value

    override fun observeActive(): Flow<List<Persona>> = personas.map { list ->
        list.filter { !it.isArchived }.sortedWith(compareBy({ it.createdAt }, { it.id }))
    }

    override fun observeArchived(): Flow<List<Persona>> = personas.map { list ->
        list.filter {
            it.isArchived
        }.sortedWith(compareByDescending<Persona> { it.archivedAt }.thenByDescending { it.id })
    }

    override fun observe(id: Long): Flow<Persona?> = personas.map { list -> list.firstOrNull { it.id == id } }

    override fun observeCurrent(): Flow<Persona?> = combine(currentIdFlow, observeActive()) { id, active ->
        active.firstOrNull { it.id == id } ?: active.firstOrNull()
    }

    override suspend fun create(name: String, goal: String?, regions: List<Region>): Long {
        val id = nextId++
        tick += 1.minutes
        personas.update { it + Persona(id, name, goal, regions, createdAt = tick) }
        return id
    }

    override suspend fun update(id: Long, name: String, goal: String?) {
        personas.update { list -> list.map { if (it.id == id) it.copy(name = name, goal = goal) else it } }
    }

    override suspend fun archive(id: Long) {
        tick += 1.minutes
        personas.update { list -> list.map { if (it.id == id) it.copy(archivedAt = tick) else it } }
    }

    override suspend fun restore(id: Long) {
        personas.update { list -> list.map { if (it.id == id) it.copy(archivedAt = null) else it } }
    }

    override suspend fun setCurrent(id: Long) {
        currentIdFlow.value = id
    }
}

/** Test persona with sensible defaults. */
fun persona(id: Long, name: String, goal: String? = null, createdMinute: Long = id, archivedMinute: Long? = null) =
    Persona(
        id = id,
        name = name,
        goal = goal,
        regions = Region.Defaults,
        createdAt = Instant.fromEpochMilliseconds(createdMinute * 60_000),
        archivedAt = archivedMinute?.let { Instant.fromEpochMilliseconds(it * 60_000) }
    )
