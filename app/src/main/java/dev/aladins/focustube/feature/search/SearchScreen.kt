package dev.aladins.focustube.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtFilterChip
import dev.aladins.focustube.core.designsystem.components.FtSearchField
import dev.aladins.focustube.core.designsystem.components.InfoBox
import dev.aladins.focustube.core.designsystem.components.LimitBanner
import dev.aladins.focustube.core.designsystem.components.PersonaAvatar
import dev.aladins.focustube.core.designsystem.components.RegionChip
import dev.aladins.focustube.core.designsystem.components.SecondaryButton
import dev.aladins.focustube.core.designsystem.components.StatusLine
import dev.aladins.focustube.core.designsystem.components.regionSummary
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.RegionSheet
import dev.aladins.focustube.core.ui.VideoListItem
import dev.aladins.focustube.core.ui.elapsedText
import dev.aladins.focustube.core.ui.errorText
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.ShortsFilter
import dev.aladins.focustube.feature.search.components.RecentSearchRow
import kotlin.time.Clock

/** Everything the Search screen can ask for. */
data class SearchActions(
    val onSearch: () -> Unit,
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
    val onFilterChange: (ShortsFilter) -> Unit,
    val onRecentClick: (RecentSearch) -> Unit,
    val onRecentRemove: (RecentSearch) -> Unit,
    val onRegionsApply: (List<Region>) -> Unit,
    val onOpenVideo: (String) -> Unit,
    val onToggleWatchLater: (String) -> Unit
)

/**
 * Classic search (12): search field with the persona avatar, region chip, All / Videos / Shorts,
 * the daily-limit banner, then Recent, results (with a status line and More results), or an
 * empty/error state. Stateless apart from whether the region sheet is open.
 */
@Composable
fun SearchScreen(state: SearchUiState, query: TextFieldState, actions: SearchActions, modifier: Modifier = Modifier) {
    val spacing = FocusTubeTheme.spacing
    var regionSheetOpen by rememberSaveable { mutableStateOf(false) }
    val now = remember(state.saved, state.recent) { Clock.System.now() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val submit: () -> Unit = {
        keyboard?.hide()
        focus.clearFocus()
        actions.onSearch()
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        Column(Modifier.padding(horizontal = spacing.screenHorizontal)) {
            FtSearchField(
                state = query,
                onSearch = submit,
                placeholder = stringResource(R.string.search_placeholder),
                clearDescription = stringResource(R.string.action_clear_search),
                leading = { state.persona?.let { PersonaAvatar(name = it.name) } }
            )
            Row(
                modifier = Modifier.padding(top = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val codes = state.regions.map { it.code }
                RegionChip(
                    regionCodes = codes,
                    onClick = { regionSheetOpen = true },
                    contentDescription = stringResource(
                        R.string.regions_chip_description,
                        regionSummary(codes, shown = codes.size)
                    )
                )
                ShortsFilter.entries.forEach { option ->
                    FtFilterChip(
                        text = stringResource(option.label),
                        selected = state.filter == option,
                        onClick = { actions.onFilterChange(option) }
                    )
                }
            }
            if (state.isLimitReached) {
                LimitBanner(
                    title = stringResource(R.string.limit_title),
                    message = stringResource(R.string.limit_message),
                    modifier = Modifier.padding(top = spacing.md)
                )
            }
        }

        val saved = state.saved
        val error = state.error
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.submittedQuery == null ->
                    RecentOrIdle(
                        state.recent,
                        actions.copy(onRecentClick = {
                            keyboard?.hide()
                            focus.clearFocus()
                            actions.onRecentClick(it)
                        })
                    )

                saved == null && state.isLoading ->
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )

                saved == null && error != null && error != AppError.QuotaExceeded -> {
                    val text = errorText(error)
                    EmptyState(
                        title = text.title,
                        message = text.message,
                        icon = text.icon,
                        actionLabel = stringResource(R.string.action_try_again),
                        onAction = actions.onSearch
                    )
                }

                // Daily limit reached on a search that isn't saved: the banner explains why; this says what to do.
                saved == null && error == AppError.QuotaExceeded ->
                    EmptyState(
                        title = stringResource(R.string.search_limit_unsaved_title),
                        message = stringResource(R.string.search_limit_unsaved_message),
                        icon = FocusTubeIcons.Clock
                    )

                saved == null -> Unit

                saved.videos.isEmpty() ->
                    EmptyState(
                        title = stringResource(R.string.search_empty_title),
                        message = stringResource(R.string.search_empty_message),
                        icon = FocusTubeIcons.Search
                    )

                else ->
                    LazyColumn(contentPadding = PaddingValues(bottom = spacing.xxl)) {
                        item {
                            StatusLine(
                                text =
                                    pluralStringResource(
                                        R.plurals.search_results_saved,
                                        state.visibleVideos.size,
                                        state.visibleVideos.size,
                                        elapsedText(saved.savedAt, now)
                                    ),
                                onRefresh = actions.onRefresh,
                                refreshEnabled = state.canRefresh,
                                refreshDescription = stringResource(R.string.action_refresh),
                                modifier = Modifier.padding(start = spacing.screenHorizontal, end = spacing.xs)
                            )
                        }
                        if (error != null && error != AppError.QuotaExceeded) {
                            item {
                                val text = errorText(error)
                                InfoBox(
                                    text = text.title + ". " + text.message,
                                    icon = text.icon,
                                    modifier = Modifier.padding(
                                        horizontal = spacing.screenHorizontal,
                                        vertical = spacing.sm
                                    )
                                )
                            }
                        }
                        if (state.visibleVideos.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(
                                        if (state.filter ==
                                            ShortsFilter.Shorts
                                        ) {
                                            R.string.search_no_shorts
                                        } else {
                                            R.string.search_no_videos
                                        }
                                    ),
                                    style = FocusTubeTheme.typography.body,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(spacing.screenHorizontal)
                                )
                            }
                        }
                        items(state.visibleVideos, key = { it.id }) { video ->
                            VideoListItem(
                                video = video,
                                marks = state.marks,
                                now = now,
                                onOpen = { actions.onOpenVideo(video.id) },
                                onToggleWatchLater = { actions.onToggleWatchLater(video.id) },
                                showShortTag = state.filter == ShortsFilter.All
                            )
                        }
                        if (saved.hasMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(spacing.lg), contentAlignment = Alignment.Center) {
                                    if (state.isLoadingMore) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    } else {
                                        SecondaryButton(
                                            text = stringResource(R.string.action_more_results),
                                            onClick = actions.onLoadMore,
                                            enabled = state.canLoadMore
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    if (regionSheetOpen) {
        RegionSheet(
            title = stringResource(R.string.regions_title),
            selected = state.regions,
            defaults = state.defaultRegions,
            onApply = {
                regionSheetOpen = false
                actions.onRegionsApply(it)
            },
            onDismiss = { regionSheetOpen = false }
        )
    }
}

@Composable
private fun RecentOrIdle(recent: List<RecentSearch>, actions: SearchActions) {
    if (recent.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.search_idle_title),
            message = stringResource(R.string.search_idle_message),
            icon = FocusTubeIcons.Search
        )
        return
    }
    LazyColumn(contentPadding = PaddingValues(top = FocusTubeTheme.spacing.md)) {
        item {
            Text(
                text = stringResource(R.string.search_recent).uppercase(),
                style = FocusTubeTheme.typography.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .padding(horizontal = FocusTubeTheme.spacing.screenHorizontal)
                        .semantics { heading() }
            )
            Spacer(Modifier.height(FocusTubeTheme.spacing.xs))
        }
        items(recent, key = { it.query.lowercase() }) { item ->
            RecentSearchRow(
                query = item.query,
                onClick = { actions.onRecentClick(item) },
                onRemove = { actions.onRecentRemove(item) }
            )
        }
    }
}

/** Label of each filter chip. */
private val ShortsFilter.label: Int
    get() = when (this) {
        ShortsFilter.All -> R.string.filter_all
        ShortsFilter.Videos -> R.string.filter_videos
        ShortsFilter.Shorts -> R.string.filter_shorts
    }
