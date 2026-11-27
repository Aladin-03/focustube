package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Light Material 3 colour scheme built from the DESIGN.md tokens.
 *
 * All surfaceContainer slots are pinned to surface / surface2 so Material components never add
 * their own tonal tint. DESIGN.md has no red, so the error slots use amber.
 */
internal val LightColorScheme: ColorScheme =
    lightColorScheme(
        primary = PineLight,
        onPrimary = OnPineLight,
        primaryContainer = PineSoftLight,
        onPrimaryContainer = PineLight,
        inversePrimary = PineDark,
        secondary = PineLight,
        onSecondary = OnPineLight,
        secondaryContainer = PineSoftLight,
        onSecondaryContainer = InkLight,
        tertiary = AmberInkLight,
        onTertiary = SurfaceLight,
        tertiaryContainer = AmberSoftLight,
        onTertiaryContainer = AmberInkLight,
        background = PaperLight,
        onBackground = InkLight,
        surface = SurfaceLight,
        onSurface = InkLight,
        surfaceVariant = Surface2Light,
        onSurfaceVariant = MutedLight,
        surfaceTint = PineLight,
        inverseSurface = InkLight,
        inverseOnSurface = PaperLight,
        error = AmberInkLight,
        onError = SurfaceLight,
        errorContainer = AmberSoftLight,
        onErrorContainer = AmberInkLight,
        outline = LineLight,
        outlineVariant = LineLight,
        surfaceBright = SurfaceLight,
        surfaceDim = Surface2Light,
        surfaceContainerLowest = SurfaceLight,
        surfaceContainerLow = SurfaceLight,
        surfaceContainer = SurfaceLight,
        surfaceContainerHigh = SurfaceLight,
        surfaceContainerHighest = Surface2Light
    )

/** Dark counterpart of [LightColorScheme]. */
internal val DarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = PineDark,
        onPrimary = OnPineDark,
        primaryContainer = PineSoftDark,
        onPrimaryContainer = PineDark,
        inversePrimary = PineLight,
        secondary = PineDark,
        onSecondary = OnPineDark,
        secondaryContainer = PineSoftDark,
        onSecondaryContainer = InkDark,
        tertiary = AmberInkDark,
        onTertiary = PaperDark,
        tertiaryContainer = AmberSoftDark,
        onTertiaryContainer = AmberInkDark,
        background = PaperDark,
        onBackground = InkDark,
        surface = SurfaceDark,
        onSurface = InkDark,
        surfaceVariant = Surface2Dark,
        onSurfaceVariant = MutedDark,
        surfaceTint = PineDark,
        inverseSurface = InkDark,
        inverseOnSurface = PaperDark,
        error = AmberInkDark,
        onError = PaperDark,
        errorContainer = AmberSoftDark,
        onErrorContainer = AmberInkDark,
        outline = LineDark,
        outlineVariant = LineDark,
        surfaceBright = Surface2Dark,
        surfaceDim = PaperDark,
        surfaceContainerLowest = PaperDark,
        surfaceContainerLow = SurfaceDark,
        surfaceContainer = SurfaceDark,
        surfaceContainerHigh = SurfaceDark,
        surfaceContainerHighest = Surface2Dark
    )

/**
 * FocusTube's theme: Material 3 plus the FocusTube token extensions.
 * Follows the system light/dark setting. Dynamic colour is deliberately not used.
 */
@Composable
fun FocusTubeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val focusTubeColors = if (darkTheme) DarkFocusTubeColors else LightFocusTubeColors
    val typography = DefaultFocusTubeTypography
    CompositionLocalProvider(
        LocalFocusTubeColors provides focusTubeColors,
        LocalFocusTubeTypography provides typography,
        LocalFocusTubeShapes provides DefaultFocusTubeShapes
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = typography.toMaterialTypography(),
            shapes = FocusTubeMaterialShapes,
            content = content
        )
    }
}

/** Access to the FocusTube tokens that are not part of [MaterialTheme]. */
object FocusTubeTheme {
    /** Extra colours (amber, meter, scrim). Standard colours: `MaterialTheme.colorScheme`. */
    val colors: FocusTubeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFocusTubeColors.current

    /** Named text styles from DESIGN.md. */
    val typography: FocusTubeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFocusTubeTypography.current

    /** Named shapes from DESIGN.md. */
    val shapes: FocusTubeShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalFocusTubeShapes.current

    /** Spacing and size tokens. */
    val spacing: FocusTubeSpacing get() = FocusTubeSpacing
}
