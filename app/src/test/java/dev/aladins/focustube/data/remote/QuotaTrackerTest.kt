package dev.aladins.focustube.data.remote

import app.cash.turbine.test
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.testing.FakeQuotaDao
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotaTrackerTest {
    private val dao = FakeQuotaDao()
    private var now = Instant.parse("2026-11-27T20:00:00Z") // 12:00 Pacific
    private val tracker = QuotaTracker(
        dao,
        object : Clock {
            override fun now() = this@QuotaTrackerTest.now
        }
    )

    @Test
    fun chargesAddUpPerPacificDay() {
        tracker.chargeBlocking(100)
        tracker.chargeBlocking(1)
        assertEquals(101L, dao.rows.value["2026-11-27"])
    }

    @Test
    fun cannotSpendPastTheLimit() {
        tracker.chargeBlocking(9_900)
        assertTrue(tracker.canSpendBlocking(100))
        assertFalse(tracker.canSpendBlocking(101))
    }

    @Test
    fun newPacificDayStartsFresh() {
        tracker.markExhaustedBlocking()
        assertFalse(tracker.canSpendBlocking(1))
        now = Instant.parse("2026-11-28T08:00:00Z") // midnight Pacific (PST, UTC-8)
        assertTrue(tracker.canSpendBlocking(100))
    }

    @Test
    fun statusShowsUsageAndExhaustion() = runTest {
        tracker.chargeBlocking(1_240)
        tracker.observeToday().test {
            val status = awaitItem()
            assertEquals(1_240L, status.usedToday)
            assertFalse(status.isExhausted)
            assertEquals(Instant.parse("2026-11-28T08:00:00Z"), status.resetsAt)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
