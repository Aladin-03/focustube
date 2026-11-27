package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.PersonaLimits
import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Restores an archived persona with everything it had. It does not become current.
 * If an active persona now has the same name, the restored one gets a number ("Guitar 2") so
 * names stay unique.
 */
class RestorePersonaUseCase @Inject constructor(private val repository: PersonaRepository) {
    suspend operator fun invoke(id: Long) {
        val persona = repository.observe(id).first() ?: return
        if (!persona.isArchived) return

        val activeNames = repository.observeActive().first().map { it.name.lowercase() }.toSet()
        if (persona.name.lowercase() in activeNames) {
            val newName = uniqueName(persona.name, activeNames)
            repository.update(id, newName, persona.goal)
        }
        repository.restore(id)
    }

    private fun uniqueName(name: String, takenLowercase: Set<String>): String {
        var n = 2
        while (true) {
            val suffix = " $n"
            val base = name.take(PersonaLimits.NAME_MAX_LENGTH - suffix.length).trimEnd()
            val candidate = base + suffix
            if (candidate.lowercase() !in takenLowercase) return candidate
            n++
        }
    }
}
