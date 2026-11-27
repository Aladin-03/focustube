package dev.aladins.focustube.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.format.compactCount
import dev.aladins.focustube.core.common.time.Elapsed
import dev.aladins.focustube.domain.model.Video
import kotlin.time.Instant

/** "3 h ago", "2 days ago", "just now". */
@Composable
fun elapsedText(then: Instant, now: Instant): String {
    val elapsed = Elapsed.between(then, now)
    val n = elapsed.amount.toInt()
    return when (elapsed.unit) {
        Elapsed.Unit.JustNow -> stringResource(R.string.elapsed_just_now)
        Elapsed.Unit.Minutes -> pluralStringResource(R.plurals.elapsed_minutes, n, n)
        Elapsed.Unit.Hours -> pluralStringResource(R.plurals.elapsed_hours, n, n)
        Elapsed.Unit.Days -> pluralStringResource(R.plurals.elapsed_days, n, n)
        Elapsed.Unit.Weeks -> pluralStringResource(R.plurals.elapsed_weeks, n, n)
        Elapsed.Unit.Months -> pluralStringResource(R.plurals.elapsed_months, n, n)
        Elapsed.Unit.Years -> pluralStringResource(R.plurals.elapsed_years, n, n)
    }
}

/** "84K views". */
@Composable
fun viewsText(count: Long): String =
    pluralStringResource(R.plurals.video_views, count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), compactCount(count))

/** Meta line under a video title: "Channel · 84K views · 3 days ago" (views left out if hidden). */
@Composable
fun videoMetaText(video: Video, now: Instant): String = listOfNotNull(
    video.channelTitle.takeIf { it.isNotBlank() },
    video.viewCount?.let { viewsText(it) },
    elapsedText(video.publishedAt, now)
).joinToString(META_SEPARATOR)

/** Separator between parts of meta and status lines. */
const val META_SEPARATOR = " · "
