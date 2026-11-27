package dev.aladins.focustube.feature.onboarding

import dev.aladins.focustube.domain.model.PersonaGoalError
import dev.aladins.focustube.domain.model.PersonaNameError
import dev.aladins.focustube.domain.usecase.CreatePersonaUseCase
import dev.aladins.focustube.domain.usecase.ValidatePersonaInputUseCase
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WelcomeViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakePersonaRepository()
    private val validate = ValidatePersonaInputUseCase()
    private val viewModel = WelcomeViewModel(validate, CreatePersonaUseCase(repo, validate))

    @Test
    fun createIsDisabledUntilANameIsTyped_andEmptyErrorIsNotShownUpFront() {
        assertFalse(viewModel.uiState.value.canCreate)
        assertNull(viewModel.uiState.value.nameError)
        viewModel.onNameChange("Guitar")
        assertTrue(viewModel.uiState.value.canCreate)
    }

    @Test
    fun clearingTheNameShowsEmptyError() {
        viewModel.onNameChange("G")
        viewModel.onNameChange("")
        assertEquals(PersonaNameError.Empty, viewModel.uiState.value.nameError)
    }

    @Test
    fun tooLongInputShowsErrorsAndDisablesCreate() {
        viewModel.onNameChange("a".repeat(31))
        viewModel.onGoalChange("g".repeat(301))
        val state = viewModel.uiState.value
        assertEquals(PersonaNameError.TooLong, state.nameError)
        assertEquals(PersonaGoalError.TooLong, state.goalError)
        assertFalse(state.canCreate)
    }

    @Test
    fun createSavesPersonaAndMakesItCurrent() = runTest {
        viewModel.onNameChange("Guitar")
        viewModel.onGoalChange("Learn fingerstyle guitar. I'm a beginner.")
        viewModel.onCreate()

        val current = repo.observeCurrent().first()
        assertEquals("Guitar", current?.name)
        assertTrue(current!!.hasGoal)
        assertTrue(viewModel.uiState.value.isSaving)
    }
}
