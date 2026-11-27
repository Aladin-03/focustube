package dev.aladins.focustube.data.remote.quota

import dev.aladins.focustube.core.common.time.PacificDay
import dev.aladins.focustube.data.local.dao.QuotaDao
import dev.aladins.focustube.domain.model.QuotaStatus
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Counts YouTube units this phone spends per Pacific day (quota_log) and refuses requests that
 * would pass the daily limit. The limit is shared by everyone using the key, so YouTube's own
 * 403 quotaExceeded is the final word: it marks today as used up.
 */
@Singleton
class QuotaTracker @Inject constructor(private val dao: QuotaDao, private val clock: Clock) {
    private fun today(): LocalDate = PacificDay.dateOf(clock.now())

    /** True if spending [cost] now stays within today's limit. Blocking: call off the main thread. */
    fun canSpendBlocking(cost: Long): Boolean = dao.unitsBlocking(today().toString()) + cost <= DAILY_LIMIT

    /** Records [cost] units spent now. Blocking. */
    fun chargeBlocking(cost: Long) = dao.addBlocking(today().toString(), cost)

    /** YouTube said the quota is used up: treat today as exhausted. Blocking. */
    fun markExhaustedBlocking() {
        val date = today().toString()
        dao.ensureRowBlocking(date)
        dao.raiseToAtLeastBlocking(date, DAILY_LIMIT)
    }

    /** Debug builds only: set today's count directly (simulate the limit, or reset it). */
    fun setTodayForDebugBlocking(units: Long) {
        val date = today().toString()
        dao.ensureRowBlocking(date)
        dao.setBlocking(date, units)
    }

    /** Today's status; moves to the new day by itself at midnight Pacific. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeToday(): Flow<QuotaStatus> = pacificDays().flatMapLatest { date ->
        val resetsAt = PacificDay.nextReset(clock.now())
        dao.observeUnits(date.toString()).map { used ->
            QuotaStatus(
                usedToday = used,
                dailyLimit = DAILY_LIMIT,
                resetsAt = resetsAt,
                isExhausted =
                    used >= DAILY_LIMIT
            )
        }
    }

    private fun pacificDays(): Flow<LocalDate> = flow {
        while (true) {
            val now = clock.now()
            emit(PacificDay.dateOf(now))
            delay(PacificDay.nextReset(now) - now + 1.seconds)
        }
    }

    companion object {
        /** YouTube Data API default daily quota. */
        const val DAILY_LIMIT = 10_000L
    }
}
