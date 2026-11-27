package dev.aladins.focustube.feature.personas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for Personas (02). Tapping the current persona just goes back. */
@Composable
fun PersonasRoute(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenArchived: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonasViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PersonasScreen(
        state = state,
        onBack = onBack,
        onSelect = { id -> if (id == state.currentId) onBack() else viewModel.onSelect(id) },
        onEdit = onEdit,
        onCreate = onCreate,
        onOpenArchived = onOpenArchived,
        modifier = modifier
    )
}
