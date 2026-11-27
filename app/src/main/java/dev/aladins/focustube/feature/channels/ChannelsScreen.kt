package dev.aladins.focustube.feature.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtSwipeToDismiss
import dev.aladins.focustube.core.designsystem.components.InfoBox
import dev.aladins.focustube.core.designsystem.components.LimitBanner
import dev.aladins.focustube.core.designsystem.components.SegmentedTabs
import dev.aladins.focustube.core.designsystem.components.StatusLine
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.VideoListItem
import dev.aladins.focustube.core.ui.dayHeaderText
import dev.aladins.focustube.core.ui.errorText
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.feature.channels.components.ChannelRow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/** Everything the My channels screen can ask for. */
data class ChannelsActions(
    val onTabChange: (ChannelsTab) -> Unit,
    val onCheckNow: () -> Unit,
    val onOpenVideo: (String) -> Unit,
    val onOpenChannel: (String) -> Unit,
    val onAddChannel: () -> Unit,
    val onToggleWatchLater: (String) -> Unit,
    val onUnfollow: (Channel) -> Unit,
    val onUndo: () -> Unit,
    val onUndoDismissed: () -> Unit
)

/**
 * My channels (08): New uploads / Channels (n). New uploads: "CHECKED TODAY 08:10" with Check now,
 * then the last 30 days by day. Channels: rows with "Latest upload"; swipe left removes one from
 * this persona (Undo). The + in the top bar opens Add channel.
 */
@Composable
fun ChannelsScreen(state: ChannelsUiState, actions: ChannelsActions, modifier: Modifier = Modifier) {
    val spacing = FocusTubeTheme.spacing
    val snackbar = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.action_undo)
    val removedText = stringResource(R.string.channels_removed, state.personaName)
    LaunchedEffect(state.undo) {
        state.undo ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(removedText, actionLabel = undoLabel, duration = SnackbarDuration.Long)
        if (result == SnackbarResult.ActionPerformed) actions.onUndo() else actions.onUndoDismissed()
    }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            SegmentedTabs(
                options = listOf(
                    stringResource(R.string.channels_tab_new_uploads),
                    stringResource(R.string.channels_tab_channels, state.channels.size)
                ),
                selectedIndex = state.tab.ordinal,
                onSelect = { actions.onTabChange(ChannelsTab.entries[it]) },
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
            )
            if (state.isLimitReached) {
                LimitBanner(
                    title = stringResource(R.string.limit_title),
                    message = stringResource(R.string.limit_message),
                    modifier = Modifier.padding(horizontal = spacing.screenHorizontal, vertical = spacing.xs)
                )
            }
            when {
                state.isLoading -> Unit

                state.channels.isEmpty() ->
                    EmptyState(
                        title = stringResource(R.string.channels_empty_title),
                        message = stringResource(R.string.channels_empty_message),
                        icon = FocusTubeIcons.Channels,
                        actionLabel = stringResource(R.string.action_add_channel),
                        onAction = actions.onAddChannel
                    )

                state.tab == ChannelsTab.NewUploads -> Feed(state, actions)

                else -> ChannelList(state, actions)
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Feed(state: ChannelsUiState, actions: ChannelsActions) {
    val spacing = FocusTubeTheme.spacing
    val now = remember(state.feed) { Clock.System.now() }
    LazyColumn(contentPadding = PaddingValues(bottom = spacing.xxl)) {
        item {
            StatusLine(
                text = checkedText(state.lastCheckedAt, state.today),
                onRefresh = actions.onCheckNow,
                refreshEnabled = state.canCheck,
                refreshDescription = stringResource(R.string.action_check_now),
                modifier = Modifier.padding(start = spacing.screenHorizontal, end = spacing.xs)
            )
        }
        val error = state.error
        if (error != null && error != AppError.QuotaExceeded) {
            item {
                val text = errorText(error)
                InfoBox(
                    text = text.title + ". " + text.message,
                    icon = text.icon,
                    modifier = Modifier.padding(horizontal = spacing.screenHorizontal)
                )
            }
        }
        if (state.feed.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.feed_empty_title),
                    message = stringResource(R.string.feed_empty_message),
                    icon = FocusTubeIcons.Channels,
                    modifier = Modifier.padding(top = spacing.xxxl)
                )
            }
        }
        state.feed.forEach { day ->
            item(key = "day-${day.date}") {
                Text(
                    text = dayHeaderText(day.date, state.today),
                    style = FocusTubeTheme.typography.dayHeader,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(
                        horizontal = spacing.screenHorizontal,
                        vertical = spacing.sm
                    ).semantics {
                        heading()
                    }
                )
            }
            items(day.items, key = { "v-${it.id}" }) { video ->
                VideoListItem(
                    video = video,
                    marks = state.marks,
                    now = now,
                    onOpen = { actions.onOpenVideo(video.id) },
                    onToggleWatchLater = { actions.onToggleWatchLater(video.id) }
                )
            }
        }
    }
}

@Composable
private fun ChannelList(state: ChannelsUiState, actions: ChannelsActions) {
    val now = remember(state.channels) { Clock.System.now() }
    LazyColumn(contentPadding = PaddingValues(bottom = FocusTubeTheme.spacing.xxl)) {
        items(state.channels, key = { it.id }) { channel ->
            FtSwipeToDismiss(
                actionLabel = stringResource(R.string.action_remove),
                onRemove = { actions.onUnfollow(channel) },
                modifier = Modifier.animateItem()
            ) {
                ChannelRow(channel = channel, now = now, showLatestUpload = true, onClick = {
                    actions.onOpenChannel(channel.id)
                })
            }
        }
    }
}

/** "Checked today 08:10" / "Checked yesterday 18:00" / "Checked Mon 24 Nov 18:00" / "Not checked yet". */
@Composable
private fun checkedText(at: Instant?, today: LocalDate): String {
    at ?: return stringResource(R.string.channels_never_checked)
    val zone = ZoneId.systemDefault()
    val local = at.toJavaInstant().atZone(zone)
    val time = DateTimeFormatter.ofLocalizedTime(
        FormatStyle.SHORT
    ).withLocale(LocalConfiguration.current.locales[0]).format(local)
    return when (local.toLocalDate()) {
        today -> stringResource(R.string.channels_checked_today, time)
        today.minusDays(1) -> stringResource(R.string.channels_checked_yesterday, time)
        else -> stringResource(R.string.channels_checked_on, dayHeaderText(local.toLocalDate(), today), time)
    }
}
