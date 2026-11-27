package dev.aladins.focustube.core.common.time

import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * YouTube's daily quota resets at midnight Pacific time (America/Los_Angeles, so daylight saving
 * time is handled). The quota log is keyed by this date.
 */
object PacificDay {
    val zone: ZoneId = ZoneId.of("America/Los_Angeles")

    /** The Pacific calendar date at [instant]. */
    fun dateOf(instant: Instant): LocalDate = instant.toJavaInstant().atZone(zone).toLocalDate()

    /** When the next quota reset happens after [instant]: the next midnight in Pacific time. */
    fun nextReset(instant: Instant): Instant =
        dateOf(instant).plusDays(1).atStartOfDay(zone).toInstant().toKotlinInstant()
}
