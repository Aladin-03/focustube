package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * Swipe a row to the left to remove it (surface2 background with a delete icon and [actionLabel]).
 * Screen readers get the same action as a custom accessibility action.
 */
@Composable
fun FtSwipeToDismiss(
    actionLabel: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Plain remember, not the saveable default: lists keep saved item state by key, so an item
    // brought back by Undo would otherwise come back already "dismissed" and remove itself again.
    val state =
        remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled) { total -> total * DISMISS_THRESHOLD } }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        onDismiss = { value -> if (value == SwipeToDismissBoxValue.EndToStart) onRemove() },
        modifier =
            modifier.semantics {
                customActions = listOf(
                    CustomAccessibilityAction(actionLabel) {
                        onRemove()
                        true
                    }
                )
            },
        backgroundContent = {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = FocusTubeTheme.spacing.xl),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = FocusTubeTheme.typography.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = FocusTubeTheme.spacing.sm)
                )
                Icon(
                    painterResource(FocusTubeIcons.Delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Row(Modifier.background(MaterialTheme.colorScheme.background)) { content() }
    }
}

/** How far (share of the row's width) a swipe must go to remove the item. */
private const val DISMISS_THRESHOLD = 0.4f
