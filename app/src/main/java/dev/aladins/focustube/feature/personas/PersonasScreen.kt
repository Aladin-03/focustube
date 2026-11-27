package dev.aladins.focustube.feature.personas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.FtTextButton
import dev.aladins.focustube.core.designsystem.components.FtTopBar
import dev.aladins.focustube.core.designsystem.components.SecondaryButton
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.feature.personas.components.PersonaCard
import kotlin.time.Instant

/** Personas (02): persona cards (current outlined), New persona, Archived (n). Stateless. */
@Composable
fun PersonasScreen(
    state: PersonasUiState,
    onBack: () -> Unit,
    onSelect: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onCreate: () -> Unit,
    onOpenArchived: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FtTopBar(title = stringResource(R.string.personas_title), onBack = onBack)
        if (state.isLoading) return@Column
        LazyColumn(
            contentPadding = PaddingValues(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            items(state.personas, key = { it.id }) { persona ->
                PersonaCard(
                    name = persona.name,
                    goal = persona.goal,
                    isCurrent = persona.id == state.currentId,
                    onClick = { onSelect(persona.id) }
                ) {
                    IconButton(onClick = { onEdit(persona.id) }) {
                        Icon(
                            painter = painterResource(FocusTubeIcons.Edit),
                            contentDescription = stringResource(R.string.personas_edit, persona.name),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                SecondaryButton(
                    text = stringResource(R.string.personas_new),
                    onClick = onCreate,
                    leadingIcon = FocusTubeIcons.Add,
                    modifier = Modifier.fillMaxWidth().padding(top = spacing.sm)
                )
            }
            if (state.archivedCount > 0) {
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        FtTextButton(
                            text = stringResource(R.string.personas_archived_link, state.archivedCount),
                            onClick = onOpenArchived
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PersonasScreenPreview() {
    val created = Instant.fromEpochMilliseconds(0)
    FocusTubeTheme {
        PersonasScreen(
            state =
                PersonasUiState(
                    isLoading = false,
                    personas =
                        listOf(
                            Persona(1, "Guitar", "Learn fingerstyle guitar. I'm a beginner.", Region.Defaults, created),
                            Persona(2, "Cooking", null, Region.Defaults, created),
                            Persona(3, "General", null, Region.Defaults, created)
                        ),
                    currentId = 1,
                    archivedCount = 1
                ),
            onBack = {},
            onSelect = {},
            onEdit = {},
            onCreate = {},
            onOpenArchived = {}
        )
    }
}
