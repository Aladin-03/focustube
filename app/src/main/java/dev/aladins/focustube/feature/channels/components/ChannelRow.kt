package dev.aladins.focustube.feature.channels.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.format.compactCount
import dev.aladins.focustube.core.designsystem.components.ChannelAvatar
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.META_SEPARATOR
import dev.aladins.focustube.core.ui.elapsedText
import dev.aladins.focustube.domain.model.Channel
import kotlin.time.Instant

/**
 * A channel in a list: avatar, name, subscribers, and (in My channels) "Latest upload 2 h ago".
 * No dots or counts (DESIGN.md). [trailing] holds an action such as Add / Added.
 */
@Composable
fun ChannelRow(
    channel: Channel,
    now: Instant,
    modifier: Modifier = Modifier,
    showLatestUpload: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    val spacing = FocusTubeTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .heightIn(min = 64.dp)
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChannelAvatar(name = channel.title, imageUrl = channel.avatarUrl)
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = channel.title,
                style = FocusTubeTheme.typography.videoTitleRow,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = channelMetaText(channel, now, showLatestUpload),
                style = FocusTubeTheme.typography.meta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        trailing()
    }
}

/** "48K subscribers · Latest upload 2 h ago". */
@Composable
fun channelMetaText(channel: Channel, now: Instant, showLatestUpload: Boolean): String {
    val parts = mutableListOf<String>()
    channel.subscriberCount?.let {
        parts +=
            pluralStringResource(
                R.plurals.channel_subscribers,
                it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                compactCount(it)
            )
    }
    if (showLatestUpload) {
        parts +=
            channel.latestUploadAt?.let { stringResource(R.string.channel_latest_upload, elapsedText(it, now)) }
                ?: stringResource(R.string.channel_no_uploads)
    }
    return parts.joinToString(META_SEPARATOR)
}
