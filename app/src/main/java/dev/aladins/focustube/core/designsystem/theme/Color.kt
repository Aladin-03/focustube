package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Raw colour tokens from docs/DESIGN.md. Only the theme files read these.
// Feature code uses MaterialTheme.colorScheme or FocusTubeTheme.colors instead.

internal val PaperLight = Color(0xFFF4F5F1)
internal val PaperDark = Color(0xFF0E1412)

internal val SurfaceLight = Color(0xFFFFFFFF)
internal val SurfaceDark = Color(0xFF161E1B)

internal val Surface2Light = Color(0xFFEBEEE8)
internal val Surface2Dark = Color(0xFF1E2824)

internal val LineLight = Color(0xFFDCE0D9)
internal val LineDark = Color(0xFF2A3531)

internal val InkLight = Color(0xFF16201C)
internal val InkDark = Color(0xFFE5ECE8)

internal val MutedLight = Color(0xFF5B6761)
internal val MutedDark = Color(0xFF9BA9A2)

internal val PineLight = Color(0xFF1E6B58)
internal val PineDark = Color(0xFF6CCBAE)

internal val OnPineLight = Color(0xFFFFFFFF)
internal val OnPineDark = Color(0xFF08221A)

internal val PineSoftLight = Color(0xFFDCEDE5)
internal val PineSoftDark = Color(0xFF1B3A31)

internal val AmberSoftLight = Color(0xFFF8E9D2)
internal val AmberSoftDark = Color(0xFF3A2B14)

internal val AmberInkLight = Color(0xFF6B3D06)
internal val AmberInkDark = Color(0xFFF2C88C)

/** Tag text on amberSoft; DESIGN.md allows a slightly brighter amber in light theme. */
internal val AmberTagInkLight = Color(0xFF8A4E08)
internal val AmberTagInkDark = AmberInkDark

internal val MeterOffLight = Color(0xFFC9D6CF)
internal val MeterOffDark = Color(0xFF34433D)

/** rgba(14,20,18,0.8): the same in both themes. */
internal val VideoScrim = Color(0xCC0E1412)
