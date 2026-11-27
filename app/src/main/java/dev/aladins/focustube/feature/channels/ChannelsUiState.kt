package dev.aladins.focustube.feature.channels

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.domain.model.DayGroup
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.usecase.LibraryMarks
import java.time.LocalDate
import kotlin.time.Instant

/** The two halves of My channels. */
enum class ChannelsTab { NewUploads, Channels }

/** A channel just removed from the persona, which Undo can put back. */
data class UnfollowUndo(val channel: Channel, val addedAt: Instant)

/** Everything My channels (08) shows, for the current persona. */
data class ChannelsUiState(
    val isLoading: Boolean = true,
    val personaName: String = "",
    val tab: ChannelsTab = ChannelsTab.NewUploads,
    /** Uploads from the last 30 days, by day, newest first. */
    val feed: List<DayGroup<Video>> = emptyList(),
    /** Followed channels, by name. */
    val channels: List<Channel> = emptyList(),
    val lastCheckedAt: Instant? = null,
    val isChecking: Boolean = false,
    val error: AppError? = null,
    val quota: QuotaStatus? = null,
    val marks: LibraryMarks = LibraryMarks(),
    val undo: UnfollowUndo? = null,
    val today: LocalDate = LocalDate.MIN
) {
    val isLimitReached: Boolean get() = quota?.isExhausted == true || error == AppError.QuotaExceeded

    val canCheck: Boolean get() = channels.isNotEmpty() && !isChecking && !isLimitReached
}
