package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * FocusTube colour tokens that Material 3's ColorScheme has no slot for.
 * Read them through [FocusTubeTheme.colors].
 */
@Immutable
data class FocusTubeColors(
    /** "New voice" tag and daily-limit banner background. */
    val amberSoft: Color,
    /** Text on [amberSoft]. */
    val amberInk: Color,
    /** Tag text on [amberSoft] (slightly brighter than [amberInk] in light theme). */
    val amberTagInk: Color,
    /** Unfilled bars of the level meter. */
    val meterOff: Color,
    /** Background of the duration badge on thumbnails. */
    val videoScrim: Color,
    /**
     * 1 dp outline of the selected SegmentedTabs option, or [Color.Unspecified] for none. Dark
     * theme only: there, surface is barely distinct from the surface2 track (needs 3:1).
     */
    val segmentSelectedOutline: Color
)

internal val LightFocusTubeColors =
    FocusTubeColors(
        amberSoft = AmberSoftLight,
        amberInk = AmberInkLight,
        amberTagInk = AmberTagInkLight,
        meterOff = MeterOffLight,
        videoScrim = VideoScrim,
        segmentSelectedOutline = Color.Unspecified
    )

internal val DarkFocusTubeColors =
    FocusTubeColors(
        amberSoft = AmberSoftDark,
        amberInk = AmberInkDark,
        amberTagInk = AmberTagInkDark,
        meterOff = MeterOffDark,
        videoScrim = VideoScrim,
        segmentSelectedOutline = MutedDark
    )

internal val LocalFocusTubeColors = staticCompositionLocalOf { LightFocusTubeColors }
