package dev.aladins.focustube.feature.personas

import app.cash.turbine.test
import dev.aladins.focustube.domain.usecase.RestorePersonaUseCase
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Personas (02) and Archived personas (04). */
class PersonasViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo =
        FakePersonaRepository(
            listOf(
                persona(1, "Guitar", goal = "Learn"),
                persona(2, "Cooking"),
                persona(3, "General"),
                persona(4, "Old", archivedMinute = 9)
            ),
            currentId = 1
        )

    @Test
    fun personasListsActiveInCreationOrderWithCurrentAndArchivedCount() = runTest {
        PersonasViewModel(repo).uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf("Guitar", "Cooking", "General"), state.personas.map { it.name })
            assertEquals(1L, state.currentId)
            assertEquals(1, state.archivedCount)
        }
    }

    @Test
    fun selectingAPersonaMakesItCurrent() = runTest {
        val vm = PersonasViewModel(repo)
        vm.uiState.test {
            vm.onSelect(3)
            assertEquals(3L, expectMostRecentItem().currentId)
        }
        assertEquals(3L, repo.observeCurrent().first()?.id)
    }

    @Test
    fun archivedListsAndRestores() = runTest {
        val vm = ArchivedPersonasViewModel(repo, RestorePersonaUseCase(repo))
        vm.uiState.test {
            assertEquals(listOf("Old"), expectMostRecentItem().personas.map { it.name })
            vm.onRestore(4)
            assertTrue(expectMostRecentItem().personas.isEmpty())
        }
        assertEquals(1L, repo.observeCurrent().first()?.id)
    }
}
