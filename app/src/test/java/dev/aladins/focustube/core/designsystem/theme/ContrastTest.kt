package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

/** Text/background token pairs must meet WCAG AA for normal text (4.5:1) in both themes. */
class ContrastTest {
    private fun channel(c: Float): Double = if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private fun assertReadable(name: String, text: Color, background: Color) {
        val ratio = contrast(text, background)
        assertTrue("$name contrast is %.2f:1, needs 4.5:1".format(ratio), ratio >= 4.5)
    }

    private fun checkTheme(
        theme: String,
        paper: Color,
        surface: Color,
        surface2: Color,
        ink: Color,
        muted: Color,
        pine: Color,
        onPine: Color,
        pineSoft: Color,
        amberSoft: Color,
        amberInk: Color,
        amberTagInk: Color
    ) {
        assertReadable("$theme ink on paper", ink, paper)
        assertReadable("$theme ink on surface", ink, surface)
        assertReadable("$theme muted on paper", muted, paper)
        assertReadable("$theme muted on surface", muted, surface)
        assertReadable("$theme muted on surface2", muted, surface2)
        assertReadable("$theme onPine on pine", onPine, pine)
        assertReadable("$theme pine on pineSoft", pine, pineSoft)
        assertReadable("$theme amberInk on amberSoft", amberInk, amberSoft)
        assertReadable("$theme amberTagInk on amberSoft", amberTagInk, amberSoft)
    }

    @Test
    fun lightThemeTextIsReadable() = checkTheme(
        "light",
        PaperLight,
        SurfaceLight,
        Surface2Light,
        InkLight,
        MutedLight,
        PineLight,
        OnPineLight,
        PineSoftLight,
        AmberSoftLight,
        AmberInkLight,
        AmberTagInkLight
    )

    @Test
    fun darkThemeTextIsReadable() = checkTheme(
        "dark",
        PaperDark,
        SurfaceDark,
        Surface2Dark,
        InkDark,
        MutedDark,
        PineDark,
        OnPineDark,
        PineSoftDark,
        AmberSoftDark,
        AmberInkDark,
        AmberTagInkDark
    )
}
