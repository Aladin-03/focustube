package dev.aladins.focustube.feature.personas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for Create / edit persona (03). Calls [onDone] once saved or archived. */
@Composable
fun EditPersonaRoute(
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPersonaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) {
        if (state.isDone) onDone()
    }
    EditPersonaScreen(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onGoalChange = viewModel::onGoalChange,
        onSave = viewModel::onSave,
        onArchive = viewModel::onArchive,
        modifier = modifier
    )
}
