package dev.aladins.focustube.domain.model

import java.time.LocalDate

/** Items of one calendar day, newest first (History, the New uploads feed). */
data class DayGroup<T>(val date: LocalDate, val items: List<T>)
