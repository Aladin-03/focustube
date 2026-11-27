package dev.aladins.focustube.feature.personas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.PersonaAvatar
import dev.aladins.focustube.core.designsystem.components.PersonaAvatarSize
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * A persona on the Personas (02) and Archived (04) screens: avatar, name, goal (or "No goal"),
 * and a [trailing] action. The current persona is outlined in pine.
 */
@Composable
fun PersonaCard(
    name: String,
    goal: String?,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val spacing = FocusTubeTheme.spacing
    val border =
        if (isCurrent) BorderStroke(2.dp, scheme.primary) else BorderStroke(1.dp, scheme.outline)
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.heightIn(
                min = 72.dp
            ).padding(start = spacing.lg, end = spacing.sm, top = spacing.md, bottom = spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PersonaAvatar(name = name, size = PersonaAvatarSize.Large)
            Spacer(Modifier.width(spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = FocusTubeTheme.typography.videoTitle,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = goal ?: stringResource(R.string.persona_no_goal),
                    style = FocusTubeTheme.typography.meta,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            trailing()
        }
    }
    val cardModifier = modifier.fillMaxWidth().semantics { selected = isCurrent }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = FocusTubeTheme.shapes.card,
            color = scheme.surface,
            border = border,
            content = content
        )
    } else {
        Surface(
            modifier = cardModifier,
            shape = FocusTubeTheme.shapes.card,
            color = scheme.surface,
            border = border,
            content = content
        )
    }
}
