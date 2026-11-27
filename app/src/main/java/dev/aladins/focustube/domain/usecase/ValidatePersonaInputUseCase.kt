package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.PersonaGoalError
import dev.aladins.focustube.domain.model.PersonaInputErrors
import dev.aladins.focustube.domain.model.PersonaLimits
import dev.aladins.focustube.domain.model.PersonaNameError
import javax.inject.Inject

/**
 * Checks a persona's name and goal: name required, at most 30 characters and not used by another
 * active persona (ignoring case); goal optional, at most 300 characters. Both are trimmed first.
 */
class ValidatePersonaInputUseCase @Inject constructor() {
    operator fun invoke(name: String, goal: String, otherActiveNames: Collection<String>): PersonaInputErrors {
        val trimmedName = name.trim()
        val nameError =
            when {
                trimmedName.isEmpty() -> PersonaNameError.Empty
                trimmedName.length > PersonaLimits.NAME_MAX_LENGTH -> PersonaNameError.TooLong
                otherActiveNames.any { it.trim().equals(trimmedName, ignoreCase = true) } -> PersonaNameError.Taken
                else -> null
            }
        val goalError = if (goal.trim().length > PersonaLimits.GOAL_MAX_LENGTH) PersonaGoalError.TooLong else null
        return PersonaInputErrors(name = nameError, goal = goalError)
    }
}
