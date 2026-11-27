package dev.aladins.focustube.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for the Search tab (Classic search, 12). */
@Composable
fun SearchRoute(
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        query = viewModel.query,
        actions =
            SearchActions(
                onSearch = viewModel::onSearch,
                onRefresh = viewModel::onRefresh,
                onLoadMore = viewModel::onLoadMore,
                onFilterChange = viewModel::onFilterChange,
                onRecentClick = viewModel::onRecentClick,
                onRecentRemove = viewModel::onRecentRemove,
                onRegionsApply = viewModel::onRegionsApply,
                onOpenVideo = onOpenVideo
            ),
        modifier = modifier
    )
}
