package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.aladins.focustube.core.common.format.formatDuration
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * A video thumbnail: rounded image, duration badge (mono on videoScrim) bottom right, and an
 * optional pine progress bar along the bottom (Library, Milestone 4). Size it with [modifier].
 */
@Composable
fun Thumbnail(
    url: String?,
    modifier: Modifier = Modifier,
    durationSeconds: Long? = null,
    shape: Shape = FocusTubeTheme.shapes.thumbnail,
    progress: Float? = null
) {
    Box(modifier = modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        if (durationSeconds != null && durationSeconds > 0) {
            Text(
                text = formatDuration(durationSeconds),
                style = FocusTubeTheme.typography.mono.copy(fontSize = FocusTubeTheme.typography.mono.fontSize * 0.92f),
                color = Color.White,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(FocusTubeTheme.colors.videoScrim, FocusTubeTheme.shapes.tag)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
        if (progress != null) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
