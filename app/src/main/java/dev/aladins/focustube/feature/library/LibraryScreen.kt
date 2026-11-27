package dev.aladins.focustube.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtSwipeToDismiss
import dev.aladins.focustube.core.designsystem.components.FtTextButton
import dev.aladins.focustube.core.designsystem.components.SegmentedTabs
import dev.aladins.focustube.core.designsystem.components.StatusLine
import dev.aladins.focustube.core.designsystem.components.Tag
import dev.aladins.focustube.core.designsystem.components.TagStyle
import dev.aladins.focustube.core.designsystem.components.VideoRow
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.dayHeaderText
import dev.aladins.focustube.core.ui.stoppedAtText
import dev.aladins.focustube.core.ui.totalTimeText
import dev.aladins.focustube.core.ui.videoMetaText
import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.model.WatchLaterItem
import dev.aladins.focustube.domain.model.WatchProgress
import kotlin.time.Clock
import kotlin.time.Instant

/** Everything the Library screen can ask for. */
data class LibraryActions(
    val onTabChange: (LibraryTab) -> Unit,
    val onOpenVideo: (String) -> Unit,
    val onRemoveFromWatchLater: (WatchLaterItem) -> Unit,
    val onRemoveFromHistory: (HistoryEntry) -> Unit,
    val onUndo: () -> Unit,
    val onUndoDismissed: () -> Unit,
    val onClearHistory: () -> Unit
)

/**
 * Library (16): Watch later (n) / History tabs. Watch later: total time and saved videos, newest
 * first. History: grouped by day with Clear history. Rows show progress, "Watched" or
 * "Stopped at 6:50"; swipe left to remove, with Undo. Stateless apart from the dialog.
 */
@Composable
fun LibraryScreen(state: LibraryUiState, actions: LibraryActions, modifier: Modifier = Modifier) {
    val spacing = FocusTubeTheme.spacing
    val snackbar = remember { SnackbarHostState() }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val removedWatchLater = stringResource(R.string.library_removed_watch_later)
    val removedHistory = stringResource(R.string.library_removed_history)
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(state.undo) {
        val undo = state.undo ?: return@LaunchedEffect
        val message = if (undo is LibraryUndo.WatchLater) removedWatchLater else removedHistory
        val result = snackbar.showSnackbar(message, actionLabel = undoLabel, duration = SnackbarDuration.Long)
        if (result == SnackbarResult.ActionPerformed) actions.onUndo() else actions.onUndoDismissed()
    }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            SegmentedTabs(
                options =
                    listOf(
                        stringResource(R.string.library_tab_watch_later, state.watchLater.size),
                        stringResource(R.string.library_tab_history)
                    ),
                selectedIndex = state.tab.ordinal,
                onSelect = { actions.onTabChange(LibraryTab.entries[it]) },
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
            )
            if (!state.isLoading) {
                when (state.tab) {
                    LibraryTab.WatchLater -> WatchLaterList(state, actions)
                    LibraryTab.History -> HistoryList(state, actions, onClear = { confirmClear = true })
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(stringResource(R.string.clear_history_message, state.personaName)) },
            confirmButton = {
                FtTextButton(
                    text = stringResource(R.string.action_clear),
                    onClick = {
                        confirmClear = false
                        actions.onClearHistory()
                    }
                )
            },
            dismissButton = {
                FtTextButton(text = stringResource(R.string.action_cancel), onClick = {
                    confirmClear =
                        false
                })
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun WatchLaterList(state: LibraryUiState, actions: LibraryActions) {
    if (state.watchLater.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.library_watch_later_empty_title),
            message = stringResource(R.string.library_watch_later_empty_message),
            icon = FocusTubeIcons.Library
        )
        return
    }
    val now = remember(state.watchLater) { Clock.System.now() }
    LazyColumn(contentPadding = PaddingValues(bottom = FocusTubeTheme.spacing.xxl)) {
        item {
            StatusLine(
                text =
                    pluralStringResource(
                        R.plurals.library_watch_later_total,
                        state.watchLater.size,
                        state.watchLater.size,
                        totalTimeText(state.watchLaterTotalSeconds)
                    ),
                modifier = Modifier.padding(horizontal = FocusTubeTheme.spacing.screenHorizontal)
            )
        }
        items(state.watchLater, key = { it.video.id }) { item ->
            FtSwipeToDismiss(
                actionLabel = stringResource(R.string.action_remove),
                onRemove = { actions.onRemoveFromWatchLater(item) },
                modifier = Modifier.animateItem()
            ) {
                LibraryRow(item.video, item.progress, now, onClick = { actions.onOpenVideo(item.video.id) })
            }
        }
    }
}

@Composable
private fun HistoryList(state: LibraryUiState, actions: LibraryActions, onClear: () -> Unit) {
    if (state.historyDays.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.library_history_empty_title),
            message = stringResource(R.string.library_history_empty_message),
            icon = FocusTubeIcons.Recent
        )
        return
    }
    val now = remember(state.historyDays) { Clock.System.now() }
    LazyColumn(contentPadding = PaddingValues(bottom = FocusTubeTheme.spacing.xxl)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = FocusTubeTheme.spacing.sm)) {
                Box(Modifier.weight(1f))
                FtTextButton(text = stringResource(R.string.action_clear_history), onClick = onClear)
            }
        }
        state.historyDays.forEach { day ->
            item(key = "day-${day.date}") {
                Text(
                    text = dayHeaderText(day.date, state.today),
                    style = FocusTubeTheme.typography.dayHeader,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = FocusTubeTheme.spacing.screenHorizontal,
                                vertical = FocusTubeTheme.spacing.sm
                            )
                            .semantics { heading() }
                )
            }
            items(day.entries, key = { "h-${it.video.id}" }) { entry ->
                FtSwipeToDismiss(
                    actionLabel = stringResource(R.string.action_remove),
                    onRemove = { actions.onRemoveFromHistory(entry) },
                    modifier = Modifier.animateItem()
                ) {
                    LibraryRow(entry.video, entry.progress, now, onClick = { actions.onOpenVideo(entry.video.id) })
                }
            }
        }
    }
}

/** A library video: progress bar, and a "Watched" tag or a "Stopped at 6:50" note. */
@Composable
private fun LibraryRow(video: Video, progress: WatchProgress?, now: Instant, onClick: () -> Unit) {
    VideoRow(
        title = video.title,
        meta = videoMetaText(video, now),
        thumbnailUrl = video.thumbnailUrl,
        durationSeconds = video.durationSeconds,
        progress = progress?.fraction,
        onClick = onClick,
        tags = {
            when {
                progress == null -> Unit

                progress.isWatched -> Tag(text = stringResource(R.string.tag_watched), style = TagStyle.Watched)

                progress.positionSeconds > 0 ->
                    Text(
                        text = stoppedAtText(progress.positionSeconds),
                        style = FocusTubeTheme.typography.meta,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
        }
    )
}
