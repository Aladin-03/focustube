package dev.aladins.focustube.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.model.PersonaNameError
import dev.aladins.focustube.domain.model.PersonaSaveResult
import dev.aladins.focustube.domain.usecase.CreatePersonaUseCase
import dev.aladins.focustube.domain.usecase.ValidatePersonaInputUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Welcome (01): creates the first persona. Once it exists it becomes current and the app shell
 * moves to its start tab, so this screen needs no navigation of its own.
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val validate: ValidatePersonaInputUseCase,
    private val createPersona: CreatePersonaUseCase
) : ViewModel() {
    private val state = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = state.asStateFlow()

    private var nameTouched = false

    fun onNameChange(name: String) {
        if (name.isNotBlank()) nameTouched = true
        state.update { it.copy(name = name).validated() }
    }

    fun onGoalChange(goal: String) {
        state.update { it.copy(goal = goal).validated() }
    }

    fun onCreate() {
        val current = state.value
        if (!current.canCreate) return
        state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = createPersona(current.name, current.goal)) {
                is PersonaSaveResult.Saved -> Unit

                // Stays "saving" while the app moves on.
                is PersonaSaveResult.Invalid ->
                    state.update {
                        it.copy(nameError = result.errors.name, goalError = result.errors.goal, isSaving = false)
                    }
            }
        }
    }

    /** Live validation. "Name is empty" only shows once something was typed and then cleared. */
    private fun WelcomeUiState.validated(): WelcomeUiState {
        val errors = validate(name, goal, otherActiveNames = emptyList())
        val nameError = errors.name.takeUnless { it == PersonaNameError.Empty && !nameTouched }
        return copy(nameError = nameError, goalError = errors.goal)
    }
}
