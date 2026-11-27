package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.PersonaSaveResult
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Creates a persona with the default regions and makes it the current persona.
 * Name and goal are trimmed; a blank goal means no goal.
 */
class CreatePersonaUseCase @Inject constructor(
    private val repository: PersonaRepository,
    private val validate: ValidatePersonaInputUseCase
) {
    suspend operator fun invoke(name: String, goal: String): PersonaSaveResult {
        val activeNames = repository.observeActive().first().map { it.name }
        val errors = validate(name, goal, activeNames)
        if (!errors.isValid) return PersonaSaveResult.Invalid(errors)

        val id = repository.create(name.trim(), goal.trim().ifEmpty { null }, Region.Defaults)
        repository.setCurrent(id)
        return PersonaSaveResult.Saved(id)
    }
}
