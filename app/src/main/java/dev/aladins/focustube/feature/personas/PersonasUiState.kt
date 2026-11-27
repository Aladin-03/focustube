package dev.aladins.focustube.feature.personas

import dev.aladins.focustube.domain.model.Persona

/** Everything Personas (02) shows. */
data class PersonasUiState(
    val isLoading: Boolean = true,
    /** Active personas in creation order. */
    val personas: List<Persona> = emptyList(),
    val currentId: Long? = null,
    val archivedCount: Int = 0
)
