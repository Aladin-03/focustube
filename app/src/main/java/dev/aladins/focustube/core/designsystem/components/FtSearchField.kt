package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/**
 * The rounded search field: [leading] (the persona avatar), the text, and a clear button.
 * The keyboard's search key calls [onSearch]. The text lives in [state] (owned by the ViewModel).
 */
@Composable
fun FtSearchField(
    state: TextFieldState,
    onSearch: () -> Unit,
    placeholder: String,
    clearDescription: String,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val textStyle = FocusTubeTheme.typography.body.copy(
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        color = scheme.onSurface
    )
    Surface(
        shape = FocusTubeTheme.shapes.pill,
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outline),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        Row(Modifier.padding(start = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            leading()
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f).padding(vertical = 14.dp)) {
                if (state.text.isEmpty()) {
                    Text(text = placeholder, style = textStyle, color = scheme.onSurfaceVariant, maxLines = 1)
                }
                BasicTextField(
                    state = state,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(scheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    onKeyboardAction = { onSearch() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.text.isNotEmpty()) {
                IconButton(onClick = { state.clearText() }) {
                    Icon(
                        painterResource(FocusTubeIcons.Close),
                        contentDescription = clearDescription,
                        tint = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
