package dev.aladins.focustube.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.format.formatDuration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** "Today", "Yesterday", or "Mon 24 Nov" in the phone's language. */
@Composable
fun dayHeaderText(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(R.string.day_today)
    today.minusDays(1) -> stringResource(R.string.day_yesterday)
    else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", LocalConfiguration.current.locales[0]))
}

/** Total watch time: "1 h 12 min" or "35 min" (rounded up to the minute). */
@Composable
fun totalTimeText(totalSeconds: Long): String {
    val minutes = (totalSeconds + 59) / 60
    return if (minutes >= 60) {
        stringResource(R.string.total_time_hours_minutes, (minutes / 60).toInt(), (minutes % 60).toInt())
    } else {
        stringResource(R.string.total_time_minutes, minutes.toInt())
    }
}

/** "Stopped at 6:50". */
@Composable
fun stoppedAtText(positionSeconds: Long): String =
    stringResource(R.string.library_stopped_at, formatDuration(positionSeconds))
