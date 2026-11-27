package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.HistoryDay
import dev.aladins.focustube.domain.model.HistoryEntry
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.toJavaInstant

/** Groups History by the phone's calendar day: newest day first, newest entry first within it. */
class GroupHistoryByDayUseCase @Inject constructor() {
    operator fun invoke(entries: List<HistoryEntry>, zone: ZoneId): List<HistoryDay> = entries
        .sortedByDescending { it.lastWatchedAt }
        .groupBy { it.lastWatchedAt.toJavaInstant().atZone(zone).toLocalDate() }
        .map { (date, dayEntries) -> HistoryDay(date, dayEntries) }
}
