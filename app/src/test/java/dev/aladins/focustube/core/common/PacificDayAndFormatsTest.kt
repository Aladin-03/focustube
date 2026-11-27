package dev.aladins.focustube.core.common

import dev.aladins.focustube.core.common.format.compactCount
import dev.aladins.focustube.core.common.format.countryName
import dev.aladins.focustube.core.common.format.formatDuration
import dev.aladins.focustube.core.common.format.regionLabel
import dev.aladins.focustube.core.common.time.Elapsed
import dev.aladins.focustube.core.common.time.PacificDay
import dev.aladins.focustube.core.designsystem.components.regionSummary
import java.time.LocalDate
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class PacificDayAndFormatsTest {
    @Test
    fun pacificDateChangesAtPacificMidnight_winterAndSummer() {
        // Winter (PST, UTC-8)
        assertEquals(LocalDate.parse("2026-11-27"), PacificDay.dateOf(Instant.parse("2026-11-28T07:59:59Z")))
        assertEquals(LocalDate.parse("2026-11-28"), PacificDay.dateOf(Instant.parse("2026-11-28T08:00:00Z")))
        // Summer (PDT, UTC-7)
        assertEquals(LocalDate.parse("2026-07-01"), PacificDay.dateOf(Instant.parse("2026-07-02T06:59:59Z")))
        assertEquals(LocalDate.parse("2026-07-02"), PacificDay.dateOf(Instant.parse("2026-07-02T07:00:00Z")))
    }

    @Test
    fun nextResetHandlesDaylightSavingChanges() {
        // DST ends on Sun 1 Nov 2026: the reset that night is 08:00 UTC (PST), the night before 07:00 UTC.
        assertEquals(Instant.parse("2026-11-01T07:00:00Z"), PacificDay.nextReset(Instant.parse("2026-10-31T20:00:00Z")))
        assertEquals(Instant.parse("2026-11-02T08:00:00Z"), PacificDay.nextReset(Instant.parse("2026-11-01T20:00:00Z")))
        // DST starts on Sun 8 Mar 2026.
        assertEquals(Instant.parse("2026-03-09T07:00:00Z"), PacificDay.nextReset(Instant.parse("2026-03-08T20:00:00Z")))
    }

    @Test
    fun durations() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:45", formatDuration(45))
        assertEquals("11:04", formatDuration(664))
        assertEquals("1:02:03", formatDuration(3723))
    }

    @Test
    fun compactCounts() {
        assertEquals("950", compactCount(950))
        assertEquals("8.4K", compactCount(8_480))
        assertEquals("84K", compactCount(84_000))
        assertEquals("1K", compactCount(1_000))
        assertEquals("1.2M", compactCount(1_250_000))
        assertEquals("3B", compactCount(3_000_000_000))
    }

    @Test
    fun regionLabelsAndSummary() {
        assertEquals("UK", regionLabel("GB"))
        assertEquals("DE", regionLabel("DE"))
        assertEquals("US, UK, DE +4", regionSummary(listOf("US", "GB", "DE", "FR", "ES", "IT", "NL")))
        assertEquals("NL", regionSummary(listOf("NL")))
        assertEquals("Germany", countryName("DE", Locale.ENGLISH))
    }

    @Test
    fun elapsed() {
        val now = Instant.parse("2026-11-27T12:00:00Z")
        assertEquals(Elapsed(Elapsed.Unit.JustNow, 0), Elapsed.between(now, now))
        assertEquals(Elapsed(Elapsed.Unit.Minutes, 5), Elapsed.between(now - 5.minutes, now))
        assertEquals(Elapsed(Elapsed.Unit.Hours, 3), Elapsed.between(now - 3.hours, now))
        assertEquals(Elapsed(Elapsed.Unit.Days, 2), Elapsed.between(now - 2.days, now))
        assertEquals(Elapsed(Elapsed.Unit.Weeks, 3), Elapsed.between(now - 22.days, now))
        assertEquals(Elapsed(Elapsed.Unit.Years, 1), Elapsed.between(now - 400.days, now))
    }
}
