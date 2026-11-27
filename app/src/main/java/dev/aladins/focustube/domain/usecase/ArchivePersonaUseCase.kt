package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Archives a persona. Everything it owns is kept. The last active persona can't be archived.
 * If the current persona is archived, the oldest remaining active persona becomes current.
 */
class ArchivePersonaUseCase @Inject constructor(private val repository: PersonaRepository) {
    /** What happened when archiving. */
    enum class Result {
        Archived,

        /** Refused: it is the only active persona. */
        LastActivePersona,

        /** Nothing to do: no active persona with this id. */
        NotFound
    }

    suspend operator fun invoke(id: Long): Result {
        val active = repository.observeActive().first()
        if (active.none { it.id == id }) return Result.NotFound
        if (active.size == 1) return Result.LastActivePersona

        val wasCurrent = repository.observeCurrent().first()?.id == id
        repository.archive(id)
        if (wasCurrent) {
            repository.setCurrent(active.first { it.id != id }.id)
        }
        return Result.Archived
    }
}
