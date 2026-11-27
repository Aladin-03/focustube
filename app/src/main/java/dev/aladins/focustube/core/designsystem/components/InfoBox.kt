package dev.aladins.focustube.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Corner radius of info and reason boxes. */
private val InfoBoxShape = RoundedCornerShape(14.dp)

/**
 * A quiet surface2 box with an icon and a short explanation, e.g. the Welcome info box or the
 * "Archiving keeps everything" note. The reason box on picks (later) builds on the same look.
 */
@Composable
fun InfoBox(text: String, modifier: Modifier = Modifier, @DrawableRes icon: Int? = FocusTubeIcons.Info) {
    val spacing = FocusTubeTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, InfoBoxShape)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = spacing.md).size(20.dp)
            )
        }
        Text(
            text = text,
            style = FocusTubeTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@PreviewLightDark
@Composable
private fun InfoBoxPreview() {
    FocusTubeTheme {
        InfoBox("With a goal, FocusTube picks videos for you and says why each one fits.")
    }
}
