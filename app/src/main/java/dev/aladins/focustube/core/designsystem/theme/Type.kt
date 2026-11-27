package dev.aladins.focustube.core.designsystem.theme

import androidx.annotation.FontRes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.aladins.focustube.R

// The three bundled variable fonts (SIL OFL, licences in assets/licenses).
// Each weight is one instance of the variable font's "wght" axis.

private fun variableFont(@FontRes resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

internal val SchibstedGrotesk =
    FontFamily(
        variableFont(R.font.schibsted_grotesk, FontWeight.SemiBold)
    )

internal val Inter =
    FontFamily(
        variableFont(R.font.inter, FontWeight.Normal),
        variableFont(R.font.inter, FontWeight.Medium),
        variableFont(R.font.inter, FontWeight.SemiBold)
    )

internal val JetBrainsMono =
    FontFamily(
        variableFont(R.font.jetbrains_mono, FontWeight.Medium)
    )

/** The default FocusTube type scale from docs/DESIGN.md. */
internal val DefaultFocusTubeTypography =
    FocusTubeTypography(
        screenTitle =
            TextStyle(
                fontFamily = SchibstedGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                letterSpacing = (-0.02).em
            ),
        displayTitle =
            TextStyle(
                fontFamily = SchibstedGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                letterSpacing = (-0.02).em
            ),
        section = TextStyle(fontFamily = SchibstedGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
        dayHeader = TextStyle(fontFamily = SchibstedGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        videoTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        videoTitleRow = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        body = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 1.45.em),
        meta = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp),
        label = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        button = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        mono =
            TextStyle(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.04.em
            )
    )

/**
 * Material 3 typography derived from [FocusTubeTypography], so stock Material components
 * (text fields, buttons, dialogs) use the same fonts as FocusTube's own components.
 */
internal fun FocusTubeTypography.toMaterialTypography(): Typography = Typography(
    displaySmall = displayTitle,
    headlineSmall = screenTitle,
    titleLarge = section,
    titleMedium = videoTitle,
    titleSmall = videoTitleRow,
    bodyLarge = body.copy(fontSize = 16.sp),
    bodyMedium = body,
    bodySmall = meta,
    labelLarge = button,
    labelMedium = label,
    labelSmall = label.copy(fontSize = 11.sp)
)
