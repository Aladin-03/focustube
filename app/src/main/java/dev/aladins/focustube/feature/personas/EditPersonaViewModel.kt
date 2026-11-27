package dev.aladins.focustube.feature.personas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.model.PersonaNameError
import dev.aladins.focustube.domain.model.PersonaSaveResult
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.usecase.ArchivePersonaUseCase
import dev.aladins.focustube.domain.usecase.CreatePersonaUseCase
import dev.aladins.focustube.domain.usecase.UpdatePersonaUseCase
import dev.aladins.focustube.domain.usecase.ValidatePersonaInputUseCase
import dev.aladins.focustube.navigation.Route
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Create / edit persona (03). Create mode has no persona id; creating makes the new persona
 * current. Edit mode loads the persona, saves changes, and can archive it (not the last one).
 */
@HiltViewModel
class EditPersonaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PersonaRepository,
    private val validate: ValidatePersonaInputUseCase,
    private val createPersona: CreatePersonaUseCase,
    private val updatePersona: UpdatePersonaUseCase,
    private val archivePersona: ArchivePersonaUseCase
) : ViewModel() {
    private val personaId: Long? = savedStateHandle.get<Long>(Route.EditPersona.PERSONA_ID_KEY)

    private val state = MutableStateFlow(EditPersonaUiState(isNew = personaId == null))
    val uiState: StateFlow<EditPersonaUiState> = state.asStateFlow()

    /** Names of the other active personas, for the "name taken" check. */
    private var otherActiveNames: List<String> = emptyList()
    private var nameTouched = personaId != null

    init {
        if (personaId != null) {
            viewModelScope.launch {
                val persona = repository.observe(personaId).first()
                if (persona == null || persona.isArchived) {
                    state.update { it.copy(isDone = true) }
                } else {
                    state.update {
                        it.copy(isLoading = false, name = persona.name, goal = persona.goal.orEmpty()).validated()
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.observeActive().collect { active ->
                otherActiveNames = active.filter { it.id != personaId }.map { it.name }
                state.update { it.copy(canArchive = personaId != null && active.size > 1).validated() }
            }
        }
    }

    fun onNameChange(name: String) {
        if (name.isNotBlank()) nameTouched = true
        state.update { it.copy(name = name).validated() }
    }

    fun onGoalChange(goal: String) {
        state.update { it.copy(goal = goal).validated() }
    }

    fun onSave() {
        val current = state.value
        if (!current.canSave) return
        state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result =
                if (personaId == null) {
                    createPersona(current.name, current.goal)
                } else {
                    updatePersona(personaId, current.name, current.goal)
                }
            when (result) {
                is PersonaSaveResult.Saved -> state.update { it.copy(isDone = true) }

                is PersonaSaveResult.Invalid ->
                    state.update {
                        it.copy(nameError = result.errors.name, goalError = result.errors.goal, isSaving = false)
                    }
            }
        }
    }

    fun onArchive() {
        val id = personaId ?: return
        if (!state.value.canArchive) return
        viewModelScope.launch {
            when (archivePersona(id)) {
                ArchivePersonaUseCase.Result.Archived, ArchivePersonaUseCase.Result.NotFound ->
                    state.update { it.copy(isDone = true) }

                ArchivePersonaUseCase.Result.LastActivePersona -> state.update { it.copy(canArchive = false) }
            }
        }
    }

    private fun EditPersonaUiState.validated(): EditPersonaUiState {
        if (isLoading) return this
        val errors = validate(name, goal, otherActiveNames)
        val nameError = errors.name.takeUnless { it == PersonaNameError.Empty && !nameTouched }
        return copy(nameError = nameError, goalError = errors.goal)
    }
}
