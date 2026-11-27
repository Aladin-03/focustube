package dev.aladins.focustube.feature.personas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Personas (02): lists active personas and switches the current one. */
@HiltViewModel
class PersonasViewModel @Inject constructor(private val repository: PersonaRepository) : ViewModel() {
    val uiState: StateFlow<PersonasUiState> =
        combine(repository.observeActive(), repository.observeCurrent(), repository.observeArchived()) {
                active,
                current,
                archived
            ->
            PersonasUiState(
                isLoading = false,
                personas = active,
                currentId = current?.id,
                archivedCount = archived.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PersonasUiState())

    /** Makes [id] current; the app then opens that persona's start tab. */
    fun onSelect(id: Long) {
        viewModelScope.launch { repository.setCurrent(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
