package dev.aladins.focustube.feature.library

import app.cash.turbine.test
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.usecase.GroupByDayUseCase
import dev.aladins.focustube.testing.FakeHistoryRepository
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeVideoRepository
import dev.aladins.focustube.testing.FakeWatchLaterRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LibraryViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val personas = FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "General")), currentId = 1)
    private val watchLater = FakeWatchLaterRepository()
    private val history = FakeHistoryRepository()
    private val videos = FakeVideoRepository()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-27T12:00:00Z")
    }

    private fun viewModel() =
        LibraryViewModel(personas, watchLater, history, videos, GroupByDayUseCase(), clock, ZoneId.of("UTC"))

    @Test
    fun showsOnlyTheCurrentPersonasLibrary_andRefreshesOnce() = runTest {
        watchLater.add(1, "a")
        watchLater.add(1, "b")
        watchLater.add(2, "other")
        history.save(1, "a", WatchProgress(400, 664, true), Instant.parse("2026-11-27T09:00:00Z"))
        history.save(1, "c", WatchProgress(50, 664, false), Instant.parse("2026-11-26T09:00:00Z"))
        history.save(2, "z", WatchProgress(50, 664, false), Instant.parse("2026-11-27T09:00:00Z"))

        viewModel().uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf("b", "a"), state.watchLater.map { it.video.id })
            assertEquals(
                listOf(LocalDate.parse("2026-11-27"), LocalDate.parse("2026-11-26")),
                state.historyDays.map {
                    it.date
                }
            )
            assertEquals(listOf("a"), state.historyDays[0].items.map { it.video.id })
            assertEquals(LocalDate.parse("2026-11-27"), state.today)
            assertEquals(1_328L, state.watchLaterTotalSeconds)
        }
        assertEquals(1, videos.refreshCalls)
    }

    @Test
    fun removeThenUndoPutsItBackInPlace() = runTest {
        watchLater.add(1, "a")
        watchLater.add(1, "b")
        val vm = viewModel()
        vm.uiState.test {
            val item = expectMostRecentItem().watchLater.last()
            vm.onRemoveFromWatchLater(item)
            val removed = expectMostRecentItem()
            assertEquals(listOf("b"), removed.watchLater.map { it.video.id })
            assertTrue(removed.undo is LibraryUndo.WatchLater)

            vm.onUndo()
            val back = expectMostRecentItem()
            assertEquals(listOf("b", "a"), back.watchLater.map { it.video.id })
            assertNull(back.undo)
        }
    }

    @Test
    fun removeFromHistoryWithUndo_andClearHistoryOnlyForThisPersona() = runTest {
        history.save(1, "a", WatchProgress(100, 664, false), Instant.parse("2026-11-27T09:00:00Z"))
        history.save(2, "z", WatchProgress(100, 664, false), Instant.parse("2026-11-27T09:00:00Z"))
        val vm = viewModel()
        vm.uiState.test {
            val entry = expectMostRecentItem().historyDays.single().items.single()
            vm.onRemoveFromHistory(entry)
            assertTrue(expectMostRecentItem().historyDays.isEmpty())
            vm.onUndo()
            assertEquals(1, expectMostRecentItem().historyDays.size)

            vm.onClearHistory()
            assertTrue(expectMostRecentItem().historyDays.isEmpty())
        }
        assertEquals(1, history.rows.value.size) // General's entry untouched
    }

    @Test
    fun tabChanges() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            vm.onTabChange(LibraryTab.History)
            assertEquals(LibraryTab.History, expectMostRecentItem().tab)
        }
    }
}
