package dev.aladins.focustube.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.Tag
import dev.aladins.focustube.core.designsystem.components.TagStyle
import dev.aladins.focustube.core.designsystem.components.VideoRow
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.usecase.LibraryMarks
import kotlin.time.Instant

/**
 * A video in a list (search results, new uploads, a channel's uploads): meta line, "Short" tag,
 * a thin progress bar if started, and the Watch later bookmark.
 */
@Composable
fun VideoListItem(
    video: Video,
    marks: LibraryMarks,
    now: Instant,
    onOpen: () -> Unit,
    onToggleWatchLater: () -> Unit,
    modifier: Modifier = Modifier,
    showShortTag: Boolean = true
) {
    VideoRow(
        title = video.title,
        meta = videoMetaText(video, now),
        thumbnailUrl = video.thumbnailUrl,
        durationSeconds = video.durationSeconds,
        onClick = onOpen,
        progress = marks.progress[video.id]?.fraction?.takeIf { it > 0f },
        modifier = modifier,
        tags = {
            if (showShortTag && video.isShort) Tag(text = stringResource(R.string.tag_short), style = TagStyle.Short)
        },
        trailing = { WatchLaterButton(isSaved = video.id in marks.savedIds, onToggle = onToggleWatchLater) }
    )
}
