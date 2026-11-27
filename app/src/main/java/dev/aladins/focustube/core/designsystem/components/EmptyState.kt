package dev.aladins.focustube.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * A centred message for screens with nothing to show: an icon block, a title, a short text, and
 * optionally a primary button and a text link below it.
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    linkLabel: String? = null,
    onLink: (() -> Unit)? = null
) {
    val spacing = FocusTubeTheme.spacing
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier =
                    Modifier
                        .size(88.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, FocusTubeTheme.shapes.card),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(spacing.xxl))
        }
        Text(
            text = title,
            style = FocusTubeTheme.typography.section,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = message,
            style = FocusTubeTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(spacing.xxl))
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
        if (linkLabel != null && onLink != null) {
            Spacer(Modifier.height(spacing.sm))
            FtTextButton(text = linkLabel, onClick = onLink)
        }
    }
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    FocusTubeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            EmptyState(
                title = "No picks without a goal",
                message = "Add a goal to this persona and FocusTube will pick videos for it.",
                icon = FocusTubeIcons.Picks,
                actionLabel = "Add a goal",
                onAction = {},
                linkLabel = "Search instead",
                onLink = {}
            )
        }
    }
}
