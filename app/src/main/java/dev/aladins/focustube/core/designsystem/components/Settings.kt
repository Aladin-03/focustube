package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** A rounded surface group of settings rows, with a mono section label above. */
@Composable
fun SettingsGroup(label: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = FocusTubeTheme.typography.mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = FocusTubeTheme.spacing.xs, bottom = FocusTubeTheme.spacing.sm)
        )
        Surface(shape = FocusTubeTheme.shapes.card, color = MaterialTheme.colorScheme.surface) {
            Column(content = content)
        }
    }
}

/** Thin divider between rows in a [SettingsGroup]. */
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(start = FocusTubeTheme.spacing.lg)
    )
}

/**
 * One settings row: title, optional [value] and [supporting] text, and a chevron when it opens
 * something ([onClick]). [trailing] replaces the chevron (e.g. a "Coming later" tag).
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val spacing = FocusTubeTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .heightIn(min = 56.dp)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = FocusTubeTheme.typography.body, color = scheme.onSurface)
            if (value != null) {
                Text(text = value, style = FocusTubeTheme.typography.meta, color = scheme.onSurfaceVariant)
            }
            supporting?.invoke(this)
        }
        when {
            trailing != null -> trailing()

            onClick != null ->
                Icon(
                    painterResource(FocusTubeIcons.ChevronRight),
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant
                )
        }
    }
}
