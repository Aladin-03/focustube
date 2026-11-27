package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Type scale matches the table in docs/DESIGN.md. */
class TypographyTest {
    private val type = DefaultFocusTubeTypography

    private fun assertStyle(name: String, style: TextStyle, family: FontFamily, sizeSp: Int, weight: FontWeight) {
        assertEquals("$name font", family, style.fontFamily)
        assertEquals("$name size", sizeSp.sp, style.fontSize)
        assertEquals("$name weight", weight, style.fontWeight)
    }

    @Test
    fun stylesMatchDesign() {
        assertStyle("screenTitle", type.screenTitle, SchibstedGrotesk, 24, FontWeight.SemiBold)
        assertEquals((-0.02).em, type.screenTitle.letterSpacing)
        assertStyle("displayTitle", type.displayTitle, SchibstedGrotesk, 32, FontWeight.SemiBold)
        assertStyle("section", type.section, SchibstedGrotesk, 20, FontWeight.SemiBold)
        assertStyle("dayHeader", type.dayHeader, SchibstedGrotesk, 15, FontWeight.SemiBold)
        assertStyle("videoTitle", type.videoTitle, Inter, 16, FontWeight.SemiBold)
        assertStyle("videoTitleRow", type.videoTitleRow, Inter, 14, FontWeight.SemiBold)
        assertStyle("body", type.body, Inter, 14, FontWeight.Normal)
        assertEquals(1.45.em, type.body.lineHeight)
        assertStyle("meta", type.meta, Inter, 13, FontWeight.Normal)
        assertStyle("label", type.label, Inter, 12, FontWeight.Medium)
        assertStyle("mono", type.mono, JetBrainsMono, 12, FontWeight.Medium)
        assertEquals(0.04.em, type.mono.letterSpacing)
    }

    @Test
    fun materialTypographyUsesFocusTubeStyles() {
        val material = type.toMaterialTypography()
        assertEquals(type.screenTitle, material.headlineSmall)
        assertEquals(type.body, material.bodyMedium)
        assertEquals(type.meta, material.bodySmall)
        assertEquals(type.button, material.labelLarge)
    }
}
