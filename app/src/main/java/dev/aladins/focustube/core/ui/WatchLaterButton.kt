package dev.aladins.focustube.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons

/**
 * The Watch later bookmark, shared by search rows and the player: outlined when not saved, filled
 * pine when saved. Tapping toggles it.
 */
@Composable
fun WatchLaterButton(isSaved: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            painter = painterResource(if (isSaved) FocusTubeIcons.BookmarkFilled else FocusTubeIcons.Library),
            contentDescription = stringResource(
                if (isSaved) R.string.action_remove_watch_later else R.string.action_save_watch_later
            ),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
