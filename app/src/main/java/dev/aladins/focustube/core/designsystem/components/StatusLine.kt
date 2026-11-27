package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Mono uppercase muted status ("48 RESULTS · SAVED 3 H AGO"), with an optional Refresh button. */
@Composable
fun StatusLine(
    text: String,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
    refreshEnabled: Boolean = true,
    refreshDescription: String? = null
) {
    Row(modifier = modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.uppercase(),
            style = FocusTubeTheme.typography.mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        if (onRefresh != null) {
            IconButton(onClick = onRefresh, enabled = refreshEnabled) {
                Icon(
                    painter = painterResource(FocusTubeIcons.Refresh),
                    contentDescription = refreshDescription,
                    tint = with(MaterialTheme.colorScheme) { if (refreshEnabled) onBackground else outline }
                )
            }
        }
    }
}
