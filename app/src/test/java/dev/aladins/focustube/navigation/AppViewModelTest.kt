package dev.aladins.focustube.navigation

import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun noPersonaNeedsWelcome() {
        assertEquals(AppUiState.NeedsWelcome, AppViewModel(FakePersonaRepository()).uiState.value)
    }

    @Test
    fun readyWithCurrentPersona_andFollowsSwitches() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "General")), currentId = 2)
        val vm = AppViewModel(repo)
        assertEquals("General", (vm.uiState.value as AppUiState.Ready).persona.name)
        repo.setCurrent(1)
        assertEquals("Guitar", (vm.uiState.value as AppUiState.Ready).persona.name)
    }
}
