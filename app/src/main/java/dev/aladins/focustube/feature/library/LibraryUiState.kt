package dev.aladins.focustube.feature.library

import dev.aladins.focustube.domain.model.DayGroup
import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.WatchLaterItem
import java.time.LocalDate

/** The two halves of the Library. */
enum class LibraryTab { WatchLater, History }

/** Something just removed that Undo can put back. */
sealed interface LibraryUndo {
    data class WatchLater(val item: WatchLaterItem) : LibraryUndo

    data class History(val entry: HistoryEntry) : LibraryUndo
}

/** Everything the Library (16) shows, for the current persona. */
data class LibraryUiState(
    val isLoading: Boolean = true,
    val personaName: String = "",
    val tab: LibraryTab = LibraryTab.WatchLater,
    /** Newest first. */
    val watchLater: List<WatchLaterItem> = emptyList(),
    /** Newest day first. */
    val historyDays: List<DayGroup<HistoryEntry>> = emptyList(),
    val today: LocalDate = LocalDate.MIN,
    val undo: LibraryUndo? = null
) {
    /** Total length of everything in Watch later (the status line). */
    val watchLaterTotalSeconds: Long get() = watchLater.sumOf { it.video.durationSeconds }
}
