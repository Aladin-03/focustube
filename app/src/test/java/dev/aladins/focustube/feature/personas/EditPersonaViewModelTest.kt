package dev.aladins.focustube.feature.personas

import androidx.lifecycle.SavedStateHandle
import dev.aladins.focustube.domain.model.PersonaNameError
import dev.aladins.focustube.domain.usecase.ArchivePersonaUseCase
import dev.aladins.focustube.domain.usecase.CreatePersonaUseCase
import dev.aladins.focustube.domain.usecase.UpdatePersonaUseCase
import dev.aladins.focustube.domain.usecase.ValidatePersonaInputUseCase
import dev.aladins.focustube.navigation.Route
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeSettingsRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditPersonaViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val validate = ValidatePersonaInputUseCase()

    private fun viewModel(repo: FakePersonaRepository, personaId: Long? = null) = EditPersonaViewModel(
        savedStateHandle = SavedStateHandle(
            personaId?.let {
                mapOf(Route.EditPersona.PERSONA_ID_KEY to it)
            } ?: emptyMap()
        ),
        repository = repo,
        validate = validate,
        createPersona = CreatePersonaUseCase(repo, validate, FakeSettingsRepository()),
        updatePersona = UpdatePersonaUseCase(repo, validate),
        archivePersona = ArchivePersonaUseCase(repo)
    )

    @Test
    fun createModeStartsEmpty_cannotArchive_andCreatingSwitchesPersona() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar", goal = "Learn")), currentId = 1)
        val vm = viewModel(repo)
        assertTrue(vm.uiState.value.isNew)
        assertFalse(vm.uiState.value.canArchive)

        vm.onNameChange("General")
        vm.onSave()

        assertTrue(vm.uiState.value.isDone)
        assertEquals("General", repo.observeCurrent().first()?.name)
        assertNull(repo.observeCurrent().first()?.goal)
    }

    @Test
    fun duplicateNameIsShownLive() {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar")))
        val vm = viewModel(repo)
        vm.onNameChange("guitar")
        assertEquals(PersonaNameError.Taken, vm.uiState.value.nameError)
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun editModeLoadsPersonaAndSavesChanges() = runTest {
        val repo =
            FakePersonaRepository(listOf(persona(1, "Guitar", goal = "Learn"), persona(2, "Cooking")), currentId = 1)
        val vm = viewModel(repo, personaId = 1)
        val loaded = vm.uiState.value
        assertFalse(loaded.isLoading)
        assertEquals("Guitar", loaded.name)
        assertEquals("Learn", loaded.goal)
        assertTrue(loaded.canArchive)

        vm.onGoalChange("Learn fingerstyle guitar. I'm a beginner.")
        vm.onSave()
        assertTrue(vm.uiState.value.isDone)
        assertEquals("Learn fingerstyle guitar. I'm a beginner.", repo.observe(1).first()?.goal)
    }

    @Test
    fun lastActivePersonaCannotBeArchived() {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar")), currentId = 1)
        val vm = viewModel(repo, personaId = 1)
        assertFalse(vm.uiState.value.canArchive)
        vm.onArchive()
        assertFalse(vm.uiState.value.isDone)
    }

    @Test
    fun archivingClosesTheScreenAndKeepsThePersona() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "Cooking")), currentId = 1)
        val vm = viewModel(repo, personaId = 2)
        vm.onArchive()
        assertTrue(vm.uiState.value.isDone)
        assertTrue(repo.observe(2).first()!!.isArchived)
    }

    @Test
    fun archivedOrMissingPersonaClosesImmediately() {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "Old", archivedMinute = 9)))
        assertTrue(viewModel(repo, personaId = 2).uiState.value.isDone)
        assertTrue(viewModel(repo, personaId = 99).uiState.value.isDone)
    }
}
