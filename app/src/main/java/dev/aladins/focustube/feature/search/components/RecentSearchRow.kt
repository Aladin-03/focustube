package dev.aladins.focustube.feature.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** A saved search under "Recent": tap to open it (free), ✕ to remove it. */
@Composable
fun RecentSearchRow(query: String, onClick: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = 48.dp)
                .padding(start = FocusTubeTheme.spacing.screenHorizontal, end = FocusTubeTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(FocusTubeIcons.Recent),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = FocusTubeTheme.spacing.md)
        )
        Text(
            text = query,
            style = FocusTubeTheme.typography.body,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(FocusTubeIcons.Close),
                contentDescription = stringResource(R.string.action_remove_recent, query),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
