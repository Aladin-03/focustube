package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * FocusTube text styles, named as in docs/DESIGN.md. Read them through
 * [FocusTubeTheme.typography]. Styles carry no colour: pass `color` where the
 * design says muted (for example [meta] and [mono] status lines).
 */
@Immutable
data class FocusTubeTypography(
    /** Persona name in the top bar, screen titles. */
    val screenTitle: TextStyle,
    /** Welcome heading. */
    val displayTitle: TextStyle,
    /** "Today's picks" and other section headings. */
    val section: TextStyle,
    /** Day headers in feeds and History. */
    val dayHeader: TextStyle,
    /** Video titles on large cards. */
    val videoTitle: TextStyle,
    /** Video titles in rows. */
    val videoTitleRow: TextStyle,
    /** AI reasons, descriptions. */
    val body: TextStyle,
    /** Channel, views, date. */
    val meta: TextStyle,
    /** Tab labels, chips. */
    val label: TextStyle,
    /** Button text. */
    val button: TextStyle,
    /** Durations and uppercase status lines. */
    val mono: TextStyle
)

internal val LocalFocusTubeTypography = staticCompositionLocalOf { DefaultFocusTubeTypography }
