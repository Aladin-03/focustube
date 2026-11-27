package dev.aladins.focustube.feature.personas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for Archived personas (04). */
@Composable
fun ArchivedPersonasRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchivedPersonasViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArchivedPersonasScreen(state = state, onBack = onBack, onRestore = viewModel::onRestore, modifier = modifier)
}
