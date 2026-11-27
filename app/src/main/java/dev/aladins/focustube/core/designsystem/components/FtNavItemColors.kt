package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

// Shared look of a tab in FtBottomBar and FtNavRail: active = pine icon in a pineSoft pill and an
// ink bold label; inactive = muted icon and label.

@Composable
internal fun ftNavigationBarItemColors(): NavigationBarItemColors {
    val scheme = MaterialTheme.colorScheme
    return NavigationBarItemDefaults.colors(
        selectedIconColor = scheme.primary,
        selectedTextColor = scheme.onBackground,
        indicatorColor = scheme.primaryContainer,
        unselectedIconColor = scheme.onSurfaceVariant,
        unselectedTextColor = scheme.onSurfaceVariant
    )
}

@Composable
internal fun ftNavigationRailItemColors(): NavigationRailItemColors {
    val scheme = MaterialTheme.colorScheme
    return NavigationRailItemDefaults.colors(
        selectedIconColor = scheme.primary,
        selectedTextColor = scheme.onBackground,
        indicatorColor = scheme.primaryContainer,
        unselectedIconColor = scheme.onSurfaceVariant,
        unselectedTextColor = scheme.onSurfaceVariant
    )
}

@Composable
internal fun FtNavIcon(icon: Painter) {
    Icon(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp))
}

@Composable
internal fun FtNavLabel(label: String, selected: Boolean) {
    Text(
        text = label,
        style = FocusTubeTheme.typography.label,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        maxLines = 1
    )
}
