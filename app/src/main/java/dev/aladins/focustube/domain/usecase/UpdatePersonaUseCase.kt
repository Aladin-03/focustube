package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.PersonaSaveResult
import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Changes a persona's name and goal after validating them (trimmed; blank goal = no goal). */
class UpdatePersonaUseCase @Inject constructor(
    private val repository: PersonaRepository,
    private val validate: ValidatePersonaInputUseCase
) {
    suspend operator fun invoke(id: Long, name: String, goal: String): PersonaSaveResult {
        val otherNames = repository.observeActive().first().filter { it.id != id }.map { it.name }
        val errors = validate(name, goal, otherNames)
        if (!errors.isValid) return PersonaSaveResult.Invalid(errors)

        repository.update(id, name.trim(), goal.trim().ifEmpty { null })
        return PersonaSaveResult.Saved(id)
    }
}
