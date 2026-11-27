package dev.aladins.focustube.feature.personas

import dev.aladins.focustube.domain.model.Persona

/** Everything Archived personas (04) shows. */
data class ArchivedPersonasUiState(
    val isLoading: Boolean = true,
    /** Most recently archived first. */
    val personas: List<Persona> = emptyList()
)
