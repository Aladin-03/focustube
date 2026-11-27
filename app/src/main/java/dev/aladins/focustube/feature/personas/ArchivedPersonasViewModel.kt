package dev.aladins.focustube.feature.personas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.usecase.RestorePersonaUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Archived personas (04): lists them and restores one (it does not become current). */
@HiltViewModel
class ArchivedPersonasViewModel @Inject constructor(
    repository: PersonaRepository,
    private val restorePersona: RestorePersonaUseCase
) : ViewModel() {
    val uiState: StateFlow<ArchivedPersonasUiState> =
        repository
            .observeArchived()
            .map { ArchivedPersonasUiState(isLoading = false, personas = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ArchivedPersonasUiState())

    fun onRestore(id: Long) {
        viewModelScope.launch { restorePersona(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
