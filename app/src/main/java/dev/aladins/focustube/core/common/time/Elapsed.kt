package dev.aladins.focustube.core.common.time

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** A rough "how long ago" for display ("3 h ago", "2 weeks ago"); words come from strings.xml. */
data class Elapsed(val unit: Unit, val amount: Long) {
    enum class Unit { JustNow, Minutes, Hours, Days, Weeks, Months, Years }

    companion object {
        /** Rounds down to the largest whole unit. Future times count as "just now". */
        fun between(then: Instant, now: Instant): Elapsed {
            val d: Duration = now - then
            return when {
                d < 1.minutes -> Elapsed(Unit.JustNow, 0)
                d < 1.hours -> Elapsed(Unit.Minutes, d.inWholeMinutes)
                d < 1.days -> Elapsed(Unit.Hours, d.inWholeHours)
                d < 7.days -> Elapsed(Unit.Days, d.inWholeDays)
                d < 30.days -> Elapsed(Unit.Weeks, d.inWholeDays / 7)
                d < 365.days -> Elapsed(Unit.Months, d.inWholeDays / 30)
                else -> Elapsed(Unit.Years, d.inWholeDays / 365)
            }
        }
    }
}
