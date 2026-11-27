package dev.aladins.focustube.core.common.format

import java.util.Locale

/** "4:05", "11:04", "1:02:03". Zero or negative gives "0:00". */
fun formatDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}

/** Short counts: 950, "8.4K", "84K", "1.2M", "3B". One decimal only below 10 of a unit. */
fun compactCount(count: Long): String {
    fun scaled(value: Double, suffix: String): String = if (value < 10) {
        val rounded = String.format(Locale.ROOT, "%.1f", Math.floor(value * 10) / 10)
        rounded.removeSuffix(".0") + suffix
    } else {
        "${value.toLong()}$suffix"
    }
    return when {
        count < 1_000 -> count.coerceAtLeast(0).toString()
        count < 1_000_000 -> scaled(count / 1_000.0, "K")
        count < 1_000_000_000 -> scaled(count / 1_000_000.0, "M")
        else -> scaled(count / 1_000_000_000.0, "B")
    }
}

/** Short label for a region code in chips: ISO codes, except the UK ("GB" -> "UK"). */
fun regionLabel(code: String): String = if (code == "GB") "UK" else code

/** The country's name in the phone's language, e.g. "Germany" for "DE". */
fun countryName(code: String, locale: Locale = Locale.getDefault()): String =
    Locale.Builder().setRegion(code).build().getDisplayCountry(locale).ifBlank { code }
