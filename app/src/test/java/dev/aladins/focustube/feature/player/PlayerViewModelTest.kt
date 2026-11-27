package dev.aladins.focustube.feature.player

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.navigation.Route
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeVideoRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import dev.aladins.focustube.testing.video
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val personas = FakePersonaRepository(listOf(persona(1, "Guitar")), currentId = 1)

    private fun viewModel(videos: FakeVideoRepository, id: String) =
        PlayerViewModel(SavedStateHandle(mapOf(Route.Player.VIDEO_ID_KEY to id)), videos, personas)

    @Test
    fun savedVideoLoadsWithoutFetching() = runTest {
        val videos = FakeVideoRepository(mapOf("v1" to video("v1")))
        viewModel(videos, "v1").uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Video v1", state.video?.title)
            assertEquals("Guitar", state.personaName)
            assertFalse(state.isLoading)
        }
        assertEquals(0, videos.fetchCalls)
    }

    @Test
    fun unsavedVideoIsFetched_andErrorsShow() = runTest {
        val videos = FakeVideoRepository().apply { fetchResult = AppResult.Failure(AppError.NotFound) }
        viewModel(videos, "gone").uiState.test {
            assertEquals(AppError.NotFound, expectMostRecentItem().error)
        }
        assertEquals(1, videos.fetchCalls)
    }

    @Test
    fun endStateAndReplay() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("s1" to video("s1", isShort = true))), "s1")
        vm.uiState.test {
            assertTrue(expectMostRecentItem().isShort)
            vm.onProgress(44f)
            vm.onEnded()
            val ended = expectMostRecentItem()
            assertTrue(ended.hasEnded)
            assertEquals(44f, ended.positionSeconds)
            vm.onReplay()
            assertFalse(expectMostRecentItem().hasEnded)
        }
    }
}
