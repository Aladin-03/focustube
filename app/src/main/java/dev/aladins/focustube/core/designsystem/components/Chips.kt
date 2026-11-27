package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.common.format.regionLabel
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

private val ChipPadding = PaddingValues(horizontal = 14.dp)

/** A fully rounded filter chip: pineSoft with pine text when selected, outlined otherwise. */
@Composable
fun FtFilterChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = FocusTubeTheme.shapes.pill,
        color = if (selected) scheme.primaryContainer else scheme.surface,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        border = if (selected) null else BorderStroke(1.dp, scheme.outline),
        modifier = modifier.heightIn(min = 36.dp).semantics {
            this.selected = selected
            role = Role.Tab
        }
    ) {
        Row(Modifier.padding(ChipPadding).heightIn(min = 36.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = FocusTubeTheme.typography.label)
        }
    }
}

/** Globe chip summarising regions: "US, UK, DE +4". Opens the region sheet. */
@Composable
fun RegionChip(
    regionCodes: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = FocusTubeTheme.shapes.pill,
        color = scheme.surface,
        contentColor = scheme.onSurface,
        border = BorderStroke(1.dp, scheme.outline),
        modifier =
            modifier.heightIn(min = 36.dp).then(
                if (contentDescription !=
                    null
                ) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
    ) {
        Row(Modifier.padding(ChipPadding).heightIn(min = 36.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(FocusTubeIcons.Globe),
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.padding(end = 6.dp).size(16.dp)
            )
            Text(text = regionSummary(regionCodes), style = FocusTubeTheme.typography.label)
        }
    }
}

/** "US, UK, DE +4": the first three labels, then how many more. */
fun regionSummary(codes: List<String>, shown: Int = 3): String {
    val labels = codes.map(::regionLabel)
    val head = labels.take(shown).joinToString(", ")
    return if (labels.size > shown) "$head +${labels.size - shown}" else head
}
