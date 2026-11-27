package dev.aladins.focustube.feature.settings

import app.cash.turbine.test
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeQuotaRepository
import dev.aladins.focustube.testing.FakeSettingsRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun showsDefaultsQuotaAndArchivedCount_andSavesNewDefaults() = runTest {
        val settings = FakeSettingsRepository()
        val vm =
            SettingsViewModel(
                settings,
                FakeQuotaRepository(used = 1_240),
                FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "Old", archivedMinute = 9)))
            )
        vm.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(Region.Defaults, state.defaultRegions)
            assertEquals(1_240L, state.quota?.usedToday)
            assertEquals(1, state.archivedCount)

            vm.onDefaultRegionsApply(listOf(Region("NL"), Region("DE")))
            assertEquals(listOf("NL", "DE"), expectMostRecentItem().defaultRegions.map { it.code })
        }
    }
}
