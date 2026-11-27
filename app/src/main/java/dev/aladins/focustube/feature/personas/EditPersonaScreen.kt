package dev.aladins.focustube.feature.personas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.InfoBox
import dev.aladins.focustube.core.designsystem.components.PrimaryButton
import dev.aladins.focustube.core.designsystem.components.SecondaryButton
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.PersonaFormFields

/**
 * Create / edit persona (03): name, goal, Save; in edit mode also Archive persona with its note.
 * The hidden-channels section arrives with "Never show this channel" in Milestone 6. Stateless.
 */
@Composable
fun EditPersonaScreen(
    state: EditPersonaUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        FtTopBar(
            title = stringResource(
                if (state.isNew) R.string.edit_persona_title_new else R.string.edit_persona_title_edit
            ),
            onBack = onBack
        )
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
        ) {
            PersonaFormFields(
                name = state.name,
                goal = state.goal,
                nameError = state.nameError,
                goalError = state.goalError,
                onNameChange = onNameChange,
                onGoalChange = onGoalChange
            )
            Spacer(Modifier.height(spacing.lg))
            PrimaryButton(
                text = stringResource(if (state.isNew) R.string.action_create_persona else R.string.action_save),
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            )
            if (!state.isNew) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = spacing.xxl)
                )
                SecondaryButton(
                    text = stringResource(R.string.action_archive_persona),
                    onClick = onArchive,
                    enabled = state.canArchive,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(spacing.md))
                InfoBox(
                    text =
                        stringResource(
                            if (state.canArchive) R.string.archive_persona_note else R.string.archive_persona_last
                        )
                )
            }
            Spacer(Modifier.height(spacing.xxl))
        }
    }
}

@PreviewLightDark
@Composable
private fun EditPersonaScreenPreview() {
    FocusTubeTheme {
        EditPersonaScreen(
            state =
                EditPersonaUiState(
                    isNew = false,
                    isLoading = false,
                    name = "Guitar",
                    goal = "Learn fingerstyle guitar. I'm a beginner.",
                    canArchive = true
                ),
            onBack = {},
            onNameChange = {},
            onGoalChange = {},
            onSave = {},
            onArchive = {}
        )
    }
}
