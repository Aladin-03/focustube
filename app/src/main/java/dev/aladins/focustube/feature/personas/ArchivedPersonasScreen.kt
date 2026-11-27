package dev.aladins.focustube.feature.personas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.SecondaryButton
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.feature.personas.components.PersonaCard
import kotlin.time.Instant

/** Archived personas (04): each with Restore; an empty state when there are none. Stateless. */
@Composable
fun ArchivedPersonasScreen(
    state: ArchivedPersonasUiState,
    onBack: () -> Unit,
    onRestore: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FtTopBar(title = stringResource(R.string.archived_title), onBack = onBack)
        when {
            state.isLoading -> Unit

            state.personas.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.archived_empty_title),
                    message = stringResource(R.string.archived_empty_message),
                    modifier = Modifier.weight(1f)
                )

            else ->
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    items(state.personas, key = { it.id }) { persona ->
                        PersonaCard(name = persona.name, goal = persona.goal) {
                            val description = stringResource(R.string.action_restore_persona, persona.name)
                            SecondaryButton(
                                text = stringResource(R.string.action_restore),
                                onClick = { onRestore(persona.id) },
                                modifier = Modifier.semantics { contentDescription = description }
                            )
                        }
                    }
                }
        }
    }
}

@PreviewLightDark
@Composable
private fun ArchivedPersonasScreenPreview() {
    FocusTubeTheme {
        ArchivedPersonasScreen(
            state =
                ArchivedPersonasUiState(
                    isLoading = false,
                    personas = listOf(Persona(2, "Cooking", null, Region.Defaults, Instant.fromEpochMilliseconds(0)))
                ),
            onBack = {},
            onRestore = {}
        )
    }
}
