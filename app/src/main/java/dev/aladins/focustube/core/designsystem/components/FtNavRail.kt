package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * Left navigation rail for wide screens (tablets, phones in landscape): the same tabs as
 * [FtBottomBar], on surface with a hairline on the right. Fill it with [FtNavRailItem]s.
 *
 * The surface runs edge to edge; [windowInsets] only pad the items away from system bars and cutouts.
 */
@Composable
fun FtNavRail(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
    content: @Composable ColumnScope.() -> Unit
) {
    Row(modifier = modifier.fillMaxHeight()) {
        NavigationRail(containerColor = MaterialTheme.colorScheme.surface, windowInsets = windowInsets) {
            Spacer(Modifier.height(FocusTubeTheme.spacing.lg))
            content()
        }
        VerticalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

/** One tab in [FtNavRail]. */
@Composable
fun FtNavRailItem(selected: Boolean, onClick: () -> Unit, icon: Painter, label: String, modifier: Modifier = Modifier) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = { FtNavIcon(icon) },
        label = { FtNavLabel(label, selected) },
        colors = ftNavigationRailItemColors(),
        modifier = modifier
    )
}

@Preview(heightDp = 400)
@Composable
private fun FtNavRailPreview() {
    FocusTubeTheme {
        FtNavRail {
            FtNavRailItem(true, {}, painterResource(FocusTubeIcons.Picks), "Picks")
            FtNavRailItem(false, {}, painterResource(FocusTubeIcons.Channels), "Channels")
            FtNavRailItem(false, {}, painterResource(FocusTubeIcons.Search), "Search")
            FtNavRailItem(false, {}, painterResource(FocusTubeIcons.Library), "Library")
        }
    }
}
