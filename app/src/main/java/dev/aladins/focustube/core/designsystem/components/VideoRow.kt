package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * A video in a list: 136x77 dp thumbnail on the left; title, [meta] and [tags] on the right.
 * Used by search, channels and the library.
 */
@Composable
fun VideoRow(
    title: String,
    meta: String,
    thumbnailUrl: String?,
    durationSeconds: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    tags: @Composable () -> Unit = {}
) {
    val spacing = FocusTubeTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm)
    ) {
        Thumbnail(
            url = thumbnailUrl,
            durationSeconds = durationSeconds,
            shape = FocusTubeTheme.shapes.thumbnailRow,
            progress = progress,
            modifier = Modifier.size(width = 136.dp, height = 77.dp)
        )
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = FocusTubeTheme.typography.videoTitleRow,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = meta,
                style = FocusTubeTheme.typography.meta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.padding(top = spacing.xs)
            ) { tags() }
        }
    }
}
