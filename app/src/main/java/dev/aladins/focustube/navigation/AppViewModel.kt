package dev.aladins.focustube.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.repository.PersonaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** What the app shell needs before it can show anything. */
sealed interface AppUiState {
    /** Still reading the database; the splash screen stays up. */
    data object Loading : AppUiState

    /** No active persona: first launch, show Welcome. */
    data object NeedsWelcome : AppUiState

    /** Show the app for [persona]. */
    data class Ready(val persona: Persona) : AppUiState
}

/** Activity-level state: which persona is current, or whether Welcome is needed. */
@HiltViewModel
class AppViewModel @Inject constructor(repository: PersonaRepository) : ViewModel() {
    val uiState: StateFlow<AppUiState> =
        repository
            .observeCurrent()
            .map { persona -> if (persona == null) AppUiState.NeedsWelcome else AppUiState.Ready(persona) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppUiState.Loading)
}
