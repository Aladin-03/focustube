package dev.aladins.focustube.feature.personas

import dev.aladins.focustube.domain.model.PersonaGoalError
import dev.aladins.focustube.domain.model.PersonaNameError

/** Everything Create / edit persona (03) shows. */
data class EditPersonaUiState(
    /** True for Create (opened from "New persona"), false for Edit. */
    val isNew: Boolean,
    val isLoading: Boolean = !isNew,
    val name: String = "",
    val goal: String = "",
    val nameError: PersonaNameError? = null,
    val goalError: PersonaGoalError? = null,
    val isSaving: Boolean = false,
    /** False for Create, and for the last active persona ("You need at least one persona"). */
    val canArchive: Boolean = false,
    /** Saved or archived: the screen should close. */
    val isDone: Boolean = false
) {
    val canSave: Boolean get() = !isLoading && name.isNotBlank() && nameError == null && goalError == null && !isSaving
}
