package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * Two (or more) options in a surface2 track; the selected one sits on surface with a light
 * shadow, plus a 1 dp muted outline in dark theme (where surface alone is too close to the track). Used for Watch later / History, New uploads / Channels, AI / Classic.
 */
@Composable
fun SegmentedTabs(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(scheme.surfaceVariant, FocusTubeTheme.shapes.pill)
                .padding(4.dp)
                .selectableGroup()
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Surface(
                shape = FocusTubeTheme.shapes.pill,
                color = if (selected) scheme.surface else Color.Transparent,
                shadowElevation = if (selected) 1.dp else 0.dp,
                border = FocusTubeTheme.colors.segmentSelectedOutline.takeIf {
                    selected && it.isSpecified
                }?.let { BorderStroke(1.dp, it) },
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.heightIn(min = 40.dp)) {
                    Text(
                        text = label,
                        style = FocusTubeTheme.typography.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SegmentedTabsPreview() {
    FocusTubeTheme { SegmentedTabs(listOf("Watch later (4)", "History"), selectedIndex = 0, onSelect = {}) }
}
