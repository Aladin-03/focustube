package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing and size tokens on the 4 dp grid from docs/DESIGN.md.
 * Read them through [FocusTubeTheme.spacing].
 */
object FocusTubeSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Left and right padding of every screen. */
    val screenHorizontal = 16.dp

    /** Content never grows wider than this; on tablets it is centred (phone-like width). */
    val contentMaxWidth = 600.dp

    /** Minimum touch target. */
    val touchTarget = 48.dp

    /** Height of the top bar. */
    val topBarHeight = 64.dp

    /** Primary button height on most screens. */
    val buttonHeight = 48.dp

    /** Primary button height on onboarding. */
    val buttonHeightLarge = 56.dp
}
