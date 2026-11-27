package dev.aladins.focustube.feature.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtSearchField
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.InfoBox
import dev.aladins.focustube.core.designsystem.components.LimitBanner
import dev.aladins.focustube.core.designsystem.components.SecondaryButton
import dev.aladins.focustube.core.designsystem.components.Tag
import dev.aladins.focustube.core.designsystem.components.TagStyle
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.errorText
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.feature.channels.components.ChannelRow
import dev.aladins.focustube.feature.channels.components.PersonaChecklistSheet
import kotlin.time.Clock

/** Entry point for Add channel (09). */
@Composable
fun AddChannelRoute(
    onBack: () -> Unit,
    onOpenChannel: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddChannelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AddChannelScreen(
        state = state,
        query = viewModel.query,
        onBack = onBack,
        onSearch = viewModel::onSearch,
        onAdd = viewModel::onAdd,
        onOpenChannel = onOpenChannel,
        onSave = viewModel::onSave,
        onDismissSheet = viewModel::onDismissSheet,
        modifier = modifier
    )
}

/**
 * Add channel (09): search field (name, @handle or link), channel rows with Add / Added, and the
 * "Add [channel] to…" persona sheet. Stateless.
 */
@Composable
fun AddChannelScreen(
    state: AddChannelUiState,
    query: TextFieldState,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onAdd: (Channel) -> Unit,
    onOpenChannel: (String) -> Unit,
    onSave: (Set<Long>) -> Unit,
    onDismissSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val now = remember(state.results) { Clock.System.now() }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        FtTopBar(title = stringResource(R.string.add_channel_title), onBack = onBack)
        Column(Modifier.padding(horizontal = spacing.screenHorizontal)) {
            FtSearchField(
                state = query,
                onSearch = {
                    keyboard?.hide()
                    focus.clearFocus()
                    onSearch()
                },
                placeholder = stringResource(R.string.add_channel_placeholder),
                clearDescription = stringResource(R.string.action_clear_search),
                leading = {
                    Icon(
                        painter = painterResource(FocusTubeIcons.Search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            if (state.isLimitReached) {
                LimitBanner(
                    title = stringResource(R.string.limit_title),
                    message = stringResource(R.string.limit_message),
                    modifier = Modifier.padding(top = spacing.md)
                )
            }
        }
        val error = state.error
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isSearching -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )

                error != null && error != AppError.QuotaExceeded -> {
                    val text = errorText(error)
                    EmptyState(title = text.title, message = text.message, icon = text.icon)
                }

                !state.hasSearched ->
                    InfoBox(
                        text = stringResource(R.string.add_channel_hint),
                        modifier = Modifier.padding(spacing.screenHorizontal)
                    )

                state.results.isEmpty() && error == null ->
                    EmptyState(
                        title = stringResource(R.string.add_channel_none_title),
                        message = stringResource(R.string.add_channel_none_message),
                        icon = FocusTubeIcons.Search
                    )

                else ->
                    LazyColumn(contentPadding = PaddingValues(vertical = spacing.sm)) {
                        items(state.results, key = { it.id }) { channel ->
                            ChannelRow(channel = channel, now = now, onClick = { onOpenChannel(channel.id) }) {
                                if (state.isAdded(channel)) {
                                    Tag(text = stringResource(R.string.add_channel_added), style = TagStyle.Watched)
                                } else {
                                    val description = stringResource(R.string.add_channel_add_named, channel.title)
                                    SecondaryButton(
                                        text = stringResource(R.string.action_add),
                                        onClick = { onAdd(channel) },
                                        modifier = Modifier.semantics { contentDescription = description }
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
    state.sheetChannel?.let { channel ->
        PersonaChecklistSheet(
            title = stringResource(R.string.add_channel_sheet_title, channel.title),
            personas = state.personas,
            initiallyTicked = state.sheetTicks(channel),
            onSave = onSave,
            onDismiss = onDismissSheet
        )
    }
}
