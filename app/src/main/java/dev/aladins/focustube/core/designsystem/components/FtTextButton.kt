package dev.aladins.focustube.core.designsystem.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** A text link in pine: secondary actions such as "Search instead" or "Archived (2)". */
@Composable
fun FtTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, style = FocusTubeTheme.typography.button)
    }
}
