package dev.aladins.focustube.feature.player.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** The player's Watch later toggle: outlined "Watch later", or pineSoft "Saved" with a filled bookmark. */
@Composable
fun WatchLaterAction(isSaved: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val state = stringResource(if (isSaved) R.string.watch_later_saved else R.string.watch_later)
    Surface(
        onClick = onToggle,
        shape = FocusTubeTheme.shapes.pill,
        color = if (isSaved) scheme.primaryContainer else scheme.surface,
        contentColor = if (isSaved) scheme.onPrimaryContainer else scheme.onSurface,
        border = if (isSaved) null else BorderStroke(1.dp, scheme.outline),
        modifier =
            modifier.heightIn(min = 44.dp).semantics {
                role = Role.Switch
                stateDescription = state
            }
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp).heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(if (isSaved) FocusTubeIcons.BookmarkFilled else FocusTubeIcons.Library),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp).size(20.dp)
            )
            Text(text = stringResource(R.string.watch_later), style = FocusTubeTheme.typography.button)
        }
    }
}
