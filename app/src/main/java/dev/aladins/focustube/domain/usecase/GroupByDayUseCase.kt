package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.DayGroup
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/** Groups items by the phone's calendar day: newest day first, newest item first within it. */
class GroupByDayUseCase @Inject constructor() {
    operator fun <T> invoke(items: List<T>, zone: ZoneId, instantOf: (T) -> Instant): List<DayGroup<T>> = items
        .sortedByDescending(instantOf)
        .groupBy { instantOf(it).toJavaInstant().atZone(zone).toLocalDate() }
        .map { (date, dayItems) -> DayGroup(date, dayItems) }
}
