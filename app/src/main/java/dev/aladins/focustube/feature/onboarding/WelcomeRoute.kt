package dev.aladins.focustube.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Entry point for Welcome (01). */
@Composable
fun WelcomeRoute(modifier: Modifier = Modifier, viewModel: WelcomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WelcomeScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onGoalChange = viewModel::onGoalChange,
        onCreate = viewModel::onCreate,
        modifier = modifier
    )
}
