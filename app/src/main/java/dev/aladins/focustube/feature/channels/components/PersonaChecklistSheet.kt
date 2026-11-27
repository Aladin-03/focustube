package dev.aladins.focustube.feature.channels.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.OptionsSheet
import dev.aladins.focustube.core.designsystem.components.PersonaAvatar
import dev.aladins.focustube.core.designsystem.components.PrimaryButton
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.domain.model.Persona

/**
 * "Add [channel] to…" / "[channel] is in…": a checkbox per active persona, then Save with the
 * ticked set. A channel can belong to several personas.
 */
@Composable
fun PersonaChecklistSheet(
    title: String,
    personas: List<Persona>,
    initiallyTicked: Set<Long>,
    onSave: (Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val ticked = remember { mutableStateListOf<Long>().apply { addAll(initiallyTicked) } }
    OptionsSheet(onDismiss = onDismiss) {
        Column(Modifier.navigationBarsPadding().padding(bottom = FocusTubeTheme.spacing.lg)) {
            Text(
                text = title,
                style = FocusTubeTheme.typography.section,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = FocusTubeTheme.spacing.xl).semantics { heading() }
            )
            Spacer(Modifier.padding(top = FocusTubeTheme.spacing.sm))
            personas.forEach { persona ->
                val checked = persona.id in ticked
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .toggleable(value = checked, role = Role.Checkbox) {
                                if (checked) ticked.remove(persona.id) else ticked.add(persona.id)
                            }
                            .heightIn(min = 56.dp)
                            .padding(horizontal = FocusTubeTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(FocusTubeTheme.spacing.sm))
                    PersonaAvatar(name = persona.name)
                    Spacer(Modifier.width(FocusTubeTheme.spacing.md))
                    Text(
                        text = persona.name,
                        style = FocusTubeTheme.typography.body,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { onSave(ticked.toSet()) },
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = FocusTubeTheme.spacing.xl,
                    vertical = FocusTubeTheme.spacing.md
                )
            )
        }
    }
}
