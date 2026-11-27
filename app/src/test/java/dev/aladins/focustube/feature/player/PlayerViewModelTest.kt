package dev.aladins.focustube.feature.player

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.usecase.MarkWatchedUseCase
import dev.aladins.focustube.navigation.Route
import dev.aladins.focustube.testing.FakeHistoryRepository
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeVideoRepository
import dev.aladins.focustube.testing.FakeWatchLaterRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import dev.aladins.focustube.testing.video
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val personas = FakePersonaRepository(listOf(persona(1, "Guitar")), currentId = 1)
    private val history = FakeHistoryRepository()
    private val watchLater = FakeWatchLaterRepository()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-27T12:00:00Z")
    }

    private fun viewModel(videos: FakeVideoRepository, id: String) = PlayerViewModel(
        SavedStateHandle(mapOf(Route.Player.VIDEO_ID_KEY to id)),
        videos,
        personas,
        watchLater,
        history,
        MarkWatchedUseCase(history, clock),
        CoroutineScope(UnconfinedTestDispatcher())
    )

    private val longVideo = video("v1") // 664 s
    private val short = video("s1", isShort = true) // 45 s

    /** Plays from [from] to [to] seconds in 0.25 s ticks, like the player does. */
    private fun PlayerViewModel.play(from: Float, to: Float) {
        var t = from
        while (t <= to) {
            onProgress(t)
            t += 0.25f
        }
    }

    @Test
    fun savedVideoLoadsWithoutFetching() = runTest {
        val videos = FakeVideoRepository(mapOf("v1" to longVideo))
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
    fun entersHistoryOnlyAfterAboutTenSecondsOfPlay() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1")
        vm.uiState.test {
            awaitItem()
            vm.play(0f, 9f)
            vm.onPaused()
            assertNull(history.getProgress(1, "v1"))
            vm.play(9.25f, 11f)
            vm.onPaused()
            assertEquals(11L, history.getProgress(1, "v1")!!.positionSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun seekingIsNotPlaying() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1")
        vm.uiState.test {
            awaitItem()
            vm.onProgress(0f)
            vm.onProgress(400f) // jump
            vm.onPaused()
            assertNull(history.getProgress(1, "v1"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fewWritesWhilePlaying_andWatchedSavedTheMomentItCrossesHalf() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1")
        vm.uiState.test {
            awaitItem()
            vm.play(0f, 180f)
            // 3 minutes at 4 ticks a second = 720 reports; saves every 15 s once in History.
            assertTrue("saves: ${history.saveCount}", history.saveCount in 10..13)
            assertFalse(history.getProgress(1, "v1")!!.isWatched)

            vm.play(180.25f, 332f)
            assertTrue(history.getProgress(1, "v1")!!.isWatched)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shortCountsAsWatchedOnlyWhenItEnds() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("s1" to short)), "s1")
        vm.uiState.test {
            awaitItem()
            vm.play(0f, 44f)
            vm.onPaused()
            assertFalse(history.getProgress(1, "s1")!!.isWatched)
            vm.onEnded()
            assertTrue(expectMostRecentItem().hasEnded)
            vm.onLeave() // leaving after the end must not overwrite the full-length save
            assertEquals(WatchProgress(45, 45, true), history.getProgress(1, "s1"))
            vm.onReplay()
            assertFalse(expectMostRecentItem().hasEnded)
        }
    }

    @Test
    fun unwatchedVideoResumesWhereItStopped_watchedOneStartsOver() = runTest {
        history.save(1, "v1", WatchProgress(200, 664, false), Instant.fromEpochMilliseconds(0))
        viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1").uiState.test {
            assertEquals(200f, expectMostRecentItem().positionSeconds)
        }
        history.save(1, "v1", WatchProgress(500, 664, true), Instant.fromEpochMilliseconds(0))
        viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1").uiState.test {
            assertEquals(0f, expectMostRecentItem().positionSeconds)
        }
    }

    @Test
    fun leavingSavesWhereItIs() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1")
        vm.uiState.test {
            awaitItem()
            vm.play(0f, 12.5f)
            vm.onLeave()
            assertEquals(12L, history.getProgress(1, "v1")!!.positionSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun watchLaterToggle() = runTest {
        val vm = viewModel(FakeVideoRepository(mapOf("v1" to longVideo)), "v1")
        vm.uiState.test {
            assertFalse(expectMostRecentItem().isInWatchLater)
            vm.onToggleWatchLater()
            assertTrue(expectMostRecentItem().isInWatchLater)
            vm.onToggleWatchLater()
            assertFalse(expectMostRecentItem().isInWatchLater)
        }
        assertTrue(watchLater.observeIds(1).first().isEmpty())
    }
}
