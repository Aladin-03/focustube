package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * A channel's round avatar; until (or if never) the image loads, its first letter on surface2.
 * Decorative: the channel name is always shown next to it.
 */
@Composable
fun ChannelAvatar(name: String, imageUrl: String?, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(FocusTubeTheme.shapes.pill)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatarLetter(name),
            style = FocusTubeTheme.typography.section.copy(
                fontSize =
                    FocusTubeTheme.typography.section.fontSize * (size.value / 56f)
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
