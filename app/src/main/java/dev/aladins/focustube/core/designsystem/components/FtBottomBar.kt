package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * Bottom tab bar for phones: surface background with a hairline on top. Fill it with
 * [FtBottomBarItem]s. On wide screens use [FtNavRail] instead.
 */
@Composable
fun FtBottomBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            content = content
        )
    }
}

/** One tab in [FtBottomBar]. */
@Composable
fun RowScope.FtBottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { FtNavIcon(icon) },
        label = { FtNavLabel(label, selected) },
        colors = ftNavigationBarItemColors(),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun FtBottomBarPreview() {
    FocusTubeTheme {
        FtBottomBar {
            FtBottomBarItem(true, {}, painterResource(FocusTubeIcons.Picks), "Picks")
            FtBottomBarItem(false, {}, painterResource(FocusTubeIcons.Channels), "Channels")
            FtBottomBarItem(false, {}, painterResource(FocusTubeIcons.Search), "Search")
            FtBottomBarItem(false, {}, painterResource(FocusTubeIcons.Library), "Library")
        }
    }
}
