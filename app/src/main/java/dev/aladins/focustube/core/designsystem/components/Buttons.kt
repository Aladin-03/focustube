package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Button heights from DESIGN.md. */
enum class FtButtonSize {
    /** 48 dp: most screens. */
    Regular,

    /** 56 dp: onboarding. */
    Large
}

@Composable
private fun FtButtonSize.height(): Dp = when (this) {
    FtButtonSize.Regular -> FocusTubeTheme.spacing.buttonHeight
    FtButtonSize.Large -> FocusTubeTheme.spacing.buttonHeightLarge
}

private val ButtonContentPadding: PaddingValues
    @Composable get() = PaddingValues(horizontal = FocusTubeTheme.spacing.xxl)

/** Filled pine button, fully rounded. The main action on a screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: FtButtonSize = FtButtonSize.Regular
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = FocusTubeTheme.shapes.pill,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
        contentPadding = ButtonContentPadding,
        modifier = modifier.heightIn(min = size.height())
    ) {
        Text(text = text, style = FocusTubeTheme.typography.button)
    }
}

/** Outlined button with a line-coloured border, fully rounded. A secondary action. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: FtButtonSize = FtButtonSize.Regular
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = FocusTubeTheme.shapes.pill,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
        contentPadding = ButtonContentPadding,
        modifier = modifier.heightIn(min = size.height())
    ) {
        Text(text = text, style = FocusTubeTheme.typography.button)
    }
}

@PreviewLightDark
@Composable
private fun ButtonsPreview() {
    FocusTubeTheme {
        Column(Modifier.padding(FocusTubeTheme.spacing.lg)) {
            PrimaryButton(text = "Create persona", onClick = {}, size = FtButtonSize.Large)
            SecondaryButton(text = "Search instead", onClick = {
            }, modifier = Modifier.padding(top = FocusTubeTheme.spacing.sm))
        }
    }
}
