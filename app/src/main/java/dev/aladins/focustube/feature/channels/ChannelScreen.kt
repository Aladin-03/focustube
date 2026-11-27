package dev.aladins.focustube.feature.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.format.compactCount
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.designsystem.components.ChannelAvatar
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtFilterChip
import dev.aladins.focustube.core.designsystem.components.FtTextButton
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.LimitBanner
import dev.aladins.focustube.core.designsystem.components.SecondaryButton
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.META_SEPARATOR
import dev.aladins.focustube.core.ui.VideoListItem
import dev.aladins.focustube.core.ui.errorText
import dev.aladins.focustube.feature.channels.components.PersonaChecklistSheet
import kotlin.time.Clock

/** Entry point for a channel page (10). */
@Composable
fun ChannelRoute(
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChannelScreen(
        state = state,
        onBack = onBack,
        onOpenVideo = onOpenVideo,
        onSort = viewModel::onSort,
        onLoadMore = viewModel::onLoadMore,
        onEdit = viewModel::onEdit,
        onDismissEdit = viewModel::onDismissEdit,
        onSaveFollowers = viewModel::onSaveFollowers,
        onToggleWatchLater = viewModel::onToggleWatchLater,
        modifier = modifier
    )
}

/**
 * Channel page (10): avatar, name, subscribers and videos, "In: Guitar, General · Edit", sort
 * chips Newest / Popular (room left for "Best for your goal" later), uploads, More uploads.
 */
@Composable
fun ChannelScreen(
    state: ChannelUiState,
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onSort: (UploadSort) -> Unit,
    onLoadMore: () -> Unit,
    onEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onSaveFollowers: (Set<Long>) -> Unit,
    onToggleWatchLater: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    val channel = state.channel
    val now = remember(state.uploads) { Clock.System.now() }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FtTopBar(title = channel?.title.orEmpty(), onBack = onBack)
        val error = state.error
        when {
            channel == null && state.isLoading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

            channel == null -> {
                val text = errorText(error ?: AppError.NotFound)
                EmptyState(title = text.title, message = text.message, icon = text.icon)
            }

            else ->
                LazyColumn(contentPadding = PaddingValues(bottom = spacing.xxl)) {
                    item {
                        Column(Modifier.padding(horizontal = spacing.screenHorizontal)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ChannelAvatar(name = channel.title, imageUrl = channel.avatarUrl, size = 64.dp)
                                Spacer(Modifier.width(spacing.lg))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = channel.title,
                                        style = FocusTubeTheme.typography.section,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.semantics { heading() }
                                    )
                                    Text(
                                        text = headerMeta(channel.subscriberCount, channel.videoCount),
                                        style = FocusTubeTheme.typography.meta,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = spacing.sm)
                            ) {
                                Text(
                                    text =
                                        if (state.followers.isEmpty()) {
                                            stringResource(R.string.channel_in_none)
                                        } else {
                                            stringResource(
                                                R.string.channel_in,
                                                state.followers.joinToString(", ") {
                                                    it.name
                                                }
                                            )
                                        },
                                    style = FocusTubeTheme.typography.body,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                FtTextButton(text = stringResource(R.string.action_edit), onClick = onEdit)
                            }
                            if (state.isLimitReached) {
                                LimitBanner(
                                    title = stringResource(R.string.limit_title),
                                    message = stringResource(R.string.limit_message),
                                    modifier = Modifier.padding(vertical = spacing.sm)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                modifier = Modifier.padding(vertical = spacing.sm)
                            ) {
                                FtFilterChip(stringResource(R.string.sort_newest), state.sort == UploadSort.Newest, {
                                    onSort(UploadSort.Newest)
                                })
                                FtFilterChip(stringResource(R.string.sort_popular), state.sort == UploadSort.Popular, {
                                    onSort(UploadSort.Popular)
                                })
                            }
                        }
                    }
                    items(state.uploads, key = { it.id }) { video ->
                        VideoListItem(
                            video = video,
                            marks = state.marks,
                            now = now,
                            onOpen = { onOpenVideo(video.id) },
                            onToggleWatchLater = { onToggleWatchLater(video.id) }
                        )
                    }
                    if (channel.hasMoreUploads) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(spacing.lg), contentAlignment = Alignment.Center) {
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                } else {
                                    SecondaryButton(
                                        text = stringResource(R.string.action_more_uploads),
                                        onClick = onLoadMore,
                                        enabled = state.canLoadMore
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
    if (state.isEditing && channel != null) {
        PersonaChecklistSheet(
            title = stringResource(R.string.channel_sheet_title, channel.title),
            personas = state.personas,
            initiallyTicked = state.followerIds,
            onSave = onSaveFollowers,
            onDismiss = onDismissEdit
        )
    }
}

/** "48K subscribers · 312 videos". */
@Composable
private fun headerMeta(subscribers: Long?, videos: Long?): String = listOfNotNull(
    subscribers?.let {
        pluralStringResource(
            R.plurals.channel_subscribers,
            it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            compactCount(it)
        )
    },
    videos?.let {
        pluralStringResource(
            R.plurals.channel_videos,
            it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            compactCount(it)
        )
    }
).joinToString(META_SEPARATOR)
