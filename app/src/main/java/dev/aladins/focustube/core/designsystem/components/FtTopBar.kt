package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * FocusTube's top bar: an optional back arrow, a title, and actions on the right.
 *
 * The title area is built for the persona switcher (Milestone 2): pass [leading] for the persona
 * avatar and [onTitleClick] to make the title a button with a chevron. Without them it is a plain
 * title, as used in Milestone 1 ("FocusTube") and on Settings.
 *
 * Applies the top and side system-bar insets itself.
 */
@Composable
fun FtTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    titleClickLabel: String? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val spacing = FocusTubeTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(TopBarInsets)
                .height(spacing.topBarHeight)
                .padding(start = if (onBack == null) spacing.screenHorizontal else spacing.xs, end = spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(FocusTubeIcons.Back),
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(spacing.xs))
        }
        TopBarTitle(
            title = title,
            onClick = onTitleClick,
            clickLabel = titleClickLabel,
            leading = leading,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

private val TopBarInsets: WindowInsets
    @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

@Composable
private fun TopBarTitle(
    title: String,
    onClick: (() -> Unit)?,
    clickLabel: String?,
    leading: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    val spacing = FocusTubeTheme.spacing
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier =
                Modifier
                    .heightIn(min = spacing.touchTarget)
                    .then(
                        if (onClick != null) {
                            Modifier.clickable(onClickLabel = clickLabel, role = Role.Button, onClick = onClick)
                        } else {
                            Modifier
                        }
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(spacing.md))
            }
            Text(
                text = title,
                style = FocusTubeTheme.typography.screenTitle,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
            if (onClick != null) {
                Icon(
                    painter = painterResource(FocusTubeIcons.ChevronDown),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.xs).size(20.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun FtTopBarPreview() {
    FocusTubeTheme {
        FtTopBar(
            title = "FocusTube",
            actions = {
                IconButton(onClick = {}) {
                    Icon(painterResource(FocusTubeIcons.Settings), contentDescription = null)
                }
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun FtTopBarBackPreview() {
    FocusTubeTheme {
        FtTopBar(title = "Settings", onBack = {})
    }
}
