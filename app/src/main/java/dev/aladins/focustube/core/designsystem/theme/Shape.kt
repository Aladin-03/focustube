package dev.aladins.focustube.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** FocusTube shapes from docs/DESIGN.md. Read them through [FocusTubeTheme.shapes]. */
@Immutable
data class FocusTubeShapes(
    /** Large thumbnails. */
    val thumbnail: Shape = RoundedCornerShape(16.dp),
    /** Thumbnails in rows. */
    val thumbnailRow: Shape = RoundedCornerShape(12.dp),
    /** Cards and settings groups. */
    val card: Shape = RoundedCornerShape(18.dp),
    /** Bottom sheets (top corners only). */
    val sheet: Shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
    /** Small uppercase tags. */
    val tag: Shape = RoundedCornerShape(6.dp),
    /** Chips, buttons, tab pills, search field: fully rounded. */
    val pill: Shape = CircleShape
)

internal val DefaultFocusTubeShapes = FocusTubeShapes()

internal val LocalFocusTubeShapes = staticCompositionLocalOf { DefaultFocusTubeShapes }

/** Material 3 shapes matched to FocusTube's corner sizes. */
internal val FocusTubeMaterialShapes =
    Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(18.dp),
        extraLarge = RoundedCornerShape(18.dp)
    )
