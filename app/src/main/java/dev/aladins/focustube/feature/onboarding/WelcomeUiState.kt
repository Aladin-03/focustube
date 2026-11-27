package dev.aladins.focustube.feature.onboarding

import dev.aladins.focustube.domain.model.PersonaGoalError
import dev.aladins.focustube.domain.model.PersonaNameError

/** Everything Welcome (01) shows. */
data class WelcomeUiState(
    val name: String = "",
    val goal: String = "",
    val nameError: PersonaNameError? = null,
    val goalError: PersonaGoalError? = null,
    val isSaving: Boolean = false
) {
    val canCreate: Boolean get() = name.isNotBlank() && nameError == null && goalError == null && !isSaving
}
