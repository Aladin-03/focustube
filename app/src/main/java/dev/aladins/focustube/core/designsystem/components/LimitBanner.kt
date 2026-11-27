package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** amberSoft banner with a clock icon: the YouTube daily limit is reached (screen 18). */
@Composable
fun LimitBanner(title: String, message: String, modifier: Modifier = Modifier) {
    val colors = FocusTubeTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.amberSoft, RoundedCornerShape(14.dp))
                .padding(FocusTubeTheme.spacing.lg)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
    ) {
        Icon(
            painter = painterResource(FocusTubeIcons.Clock),
            contentDescription = null,
            tint = colors.amberInk,
            modifier = Modifier.padding(end = FocusTubeTheme.spacing.md).size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = FocusTubeTheme.typography.body,
                fontWeight = FontWeight.SemiBold,
                color = colors.amberInk
            )
            Text(text = message, style = FocusTubeTheme.typography.body, color = colors.amberInk)
        }
    }
}
