package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks that every colour token in docs/DESIGN.md lands in the right Material slot or
 * FocusTube extension field, with the exact hex from the doc (written out here on purpose,
 * so a typo in Color.kt fails the test).
 */
class ColorSchemeTest {
    private fun assertHex(name: String, expected: Long, actual: Color) {
        assertEquals(
            "$name should be #${expected.toString(16).uppercase()}",
            Color(expected).toArgb(),
            actual.toArgb()
        )
    }

    private fun assertDesignTokens(
        scheme: ColorScheme,
        paper: Long,
        surface: Long,
        surface2: Long,
        line: Long,
        ink: Long,
        muted: Long,
        pine: Long,
        onPine: Long,
        pineSoft: Long
    ) {
        assertHex("paper (background)", paper, scheme.background)
        assertHex("surface", surface, scheme.surface)
        assertHex("surface2 (surfaceVariant)", surface2, scheme.surfaceVariant)
        assertHex("line (outline)", line, scheme.outline)
        assertHex("line (outlineVariant)", line, scheme.outlineVariant)
        assertHex("ink (onBackground)", ink, scheme.onBackground)
        assertHex("ink (onSurface)", ink, scheme.onSurface)
        assertHex("muted (onSurfaceVariant)", muted, scheme.onSurfaceVariant)
        assertHex("pine (primary)", pine, scheme.primary)
        assertHex("onPine (onPrimary)", onPine, scheme.onPrimary)
        assertHex("pineSoft (primaryContainer)", pineSoft, scheme.primaryContainer)
        // Material components must not pick up a tonal tint.
        assertHex("surfaceContainer", surface, scheme.surfaceContainer)
        assertHex("surfaceContainerHigh", surface, scheme.surfaceContainerHigh)
        assertHex("surfaceContainerHighest", surface2, scheme.surfaceContainerHighest)
    }

    @Test
    fun lightSchemeMatchesDesignTokens() {
        assertDesignTokens(
            LightColorScheme,
            paper = 0xFFF4F5F1,
            surface = 0xFFFFFFFF,
            surface2 = 0xFFEBEEE8,
            line = 0xFFDCE0D9,
            ink = 0xFF16201C,
            muted = 0xFF5B6761,
            pine = 0xFF1E6B58,
            onPine = 0xFFFFFFFF,
            pineSoft = 0xFFDCEDE5
        )
    }

    @Test
    fun darkSchemeMatchesDesignTokens() {
        assertDesignTokens(
            DarkColorScheme,
            paper = 0xFF0E1412,
            surface = 0xFF161E1B,
            surface2 = 0xFF1E2824,
            line = 0xFF2A3531,
            ink = 0xFFE5ECE8,
            muted = 0xFF9BA9A2,
            pine = 0xFF6CCBAE,
            onPine = 0xFF08221A,
            pineSoft = 0xFF1B3A31
        )
    }

    @Test
    fun lightExtensionColorsMatchDesignTokens() {
        val colors = LightFocusTubeColors
        assertHex("amberSoft", 0xFFF8E9D2, colors.amberSoft)
        assertHex("amberInk", 0xFF6B3D06, colors.amberInk)
        assertHex("amberTagInk", 0xFF8A4E08, colors.amberTagInk)
        assertHex("meterOff", 0xFFC9D6CF, colors.meterOff)
        assertHex("videoScrim", 0xCC0E1412, colors.videoScrim)
    }

    @Test
    fun darkExtensionColorsMatchDesignTokens() {
        val colors = DarkFocusTubeColors
        assertHex("amberSoft", 0xFF3A2B14, colors.amberSoft)
        assertHex("amberInk", 0xFFF2C88C, colors.amberInk)
        assertHex("amberTagInk", 0xFFF2C88C, colors.amberTagInk)
        assertHex("meterOff", 0xFF34433D, colors.meterOff)
        assertHex("videoScrim", 0xCC0E1412, colors.videoScrim)
    }
}
