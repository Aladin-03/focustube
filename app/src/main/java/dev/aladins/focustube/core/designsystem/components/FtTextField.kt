package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Text fields use 12 dp corners (search fields, later, are fully rounded). */
private val TextFieldShape = RoundedCornerShape(12.dp)

/** From this share of [maxLength] on, the character counter is shown. */
private const val COUNTER_THRESHOLD = 0.8f

/**
 * FocusTube text field: label above, surface fill, line border (pine when focused), and below it
 * either an error message or, near [maxLength], a character counter. Typing past [maxLength] is
 * allowed (so pasted text is never silently cut); the caller reports it as an error.
 */
@Composable
fun FtTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val spacing = FocusTubeTheme.spacing
    val scheme = MaterialTheme.colorScheme
    val length = value.trim().length
    val showCounter = maxLength != null && length >= maxLength * COUNTER_THRESHOLD
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = FocusTubeTheme.typography.label,
            color = scheme.onBackground,
            modifier = Modifier.padding(bottom = spacing.sm)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (error != null) Modifier.semantics { error(error) } else Modifier),
            textStyle = FocusTubeTheme.typography.body.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            placeholder = placeholder?.let { { Text(it, style = FocusTubeTheme.typography.body) } },
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            shape = TextFieldShape,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = scheme.surface,
                    unfocusedContainerColor = scheme.surface,
                    errorContainerColor = scheme.surface,
                    focusedBorderColor = scheme.primary,
                    unfocusedBorderColor = scheme.outline,
                    errorBorderColor = scheme.error,
                    focusedTextColor = scheme.onSurface,
                    unfocusedTextColor = scheme.onSurface,
                    cursorColor = scheme.primary,
                    unfocusedPlaceholderColor = scheme.onSurfaceVariant,
                    focusedPlaceholderColor = scheme.onSurfaceVariant
                )
        )
        if (error != null || showCounter) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = spacing.xs)) {
                Text(
                    text = error.orEmpty(),
                    style = FocusTubeTheme.typography.meta,
                    color = scheme.error,
                    modifier = Modifier.weight(1f)
                )
                if (showCounter) {
                    Text(
                        text = "$length/$maxLength",
                        style = FocusTubeTheme.typography.mono,
                        color = if (length > maxLength) scheme.error else scheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Spacer(Modifier.height(spacing.xs))
        }
    }
}

@PreviewLightDark
@Composable
private fun FtTextFieldPreview() {
    FocusTubeTheme {
        Column {
            FtTextField(value = "", onValueChange = {}, label = "Name", placeholder = "e.g. Guitar")
            FtTextField(
                value = "A name that is far too long for this",
                onValueChange = {},
                label = "Name",
                error = "Use 30 characters or fewer.",
                maxLength = 30
            )
        }
    }
}
