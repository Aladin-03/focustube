package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.testing.FakeHistoryRepository
import dev.aladins.focustube.testing.video
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The watched rule (REQUIREMENTS.md) and History grouping by day. */
class LibraryRulesTest {
    private val now = Instant.parse("2026-11-27T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = this@LibraryRulesTest.now
    }

    @Test
    fun videoIsWatchedFromHalfItsLength() {
        assertFalse(MarkWatchedUseCase.isWatched(331, 664, isShort = false, ended = false))
        assertTrue(MarkWatchedUseCase.isWatched(332, 664, isShort = false, ended = false))
        assertTrue(MarkWatchedUseCase.isWatched(10, 664, isShort = false, ended = true))
    }

    @Test
    fun shortIsWatchedOnlyAtTheEnd_andLiveNeverByPosition() {
        assertFalse(MarkWatchedUseCase.isWatched(44, 45, isShort = true, ended = false))
        assertTrue(MarkWatchedUseCase.isWatched(44, 45, isShort = true, ended = true))
        assertFalse(MarkWatchedUseCase.isWatched(5_000, 0, isShort = false, ended = false))
    }

    @Test
    fun watchedStaysWatchedWhenRewatched_andEndingSavesFullPosition() = runTest {
        val history = FakeHistoryRepository()
        val mark = MarkWatchedUseCase(history, clock)
        mark(1, "v1", 400, 664, isShort = false, ended = false)
        assertTrue(history.getProgress(1, "v1")!!.isWatched)

        mark(1, "v1", 20, 664, isShort = false, ended = false)
        val again = history.getProgress(1, "v1")!!
        assertTrue(again.isWatched)
        assertEquals(20L, again.positionSeconds)

        mark(1, "s1", 30, 45, isShort = true, ended = true)
        assertEquals(WatchProgress(45, 45, true), history.getProgress(1, "s1"))
    }

    @Test
    fun historyGroupsByLocalDayNewestFirst() {
        val zone = ZoneId.of("Asia/Kolkata") // UTC+5:30
        fun entry(id: String, at: String) = HistoryEntry(video(id), Instant.parse(at), WatchProgress(10, 100, false))
        val entries =
            listOf(
                entry("a", "2026-11-26T18:29:00Z"), // 23:59 on the 26th in India
                entry("b", "2026-11-26T18:31:00Z"), // 00:01 on the 27th in India
                entry("c", "2026-11-27T10:00:00Z")
            )
        val days = GroupHistoryByDayUseCase()(entries, zone)
        assertEquals(listOf(LocalDate.parse("2026-11-27"), LocalDate.parse("2026-11-26")), days.map { it.date })
        assertEquals(listOf("c", "b"), days[0].entries.map { it.video.id })
        assertEquals(listOf("a"), days[1].entries.map { it.video.id })
    }
}
