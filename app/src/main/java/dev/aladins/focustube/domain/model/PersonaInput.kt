package dev.aladins.focustube.domain.model

/** Length limits for a persona's name and goal (counted after trimming). */
object PersonaLimits {
    const val NAME_MAX_LENGTH = 30
    const val GOAL_MAX_LENGTH = 300
}

/** Why a persona name can't be saved. */
enum class PersonaNameError {
    Empty,
    TooLong,

    /** Another active persona already has this name (ignoring case). */
    Taken
}

/** Why a persona goal can't be saved. */
enum class PersonaGoalError {
    TooLong
}

/** The result of checking a persona's name and goal. */
data class PersonaInputErrors(val name: PersonaNameError? = null, val goal: PersonaGoalError? = null) {
    val isValid: Boolean get() = name == null && goal == null
}

/** Result of creating or updating a persona. */
sealed interface PersonaSaveResult {
    data class Saved(val personaId: Long) : PersonaSaveResult

    data class Invalid(val errors: PersonaInputErrors) : PersonaSaveResult
}
