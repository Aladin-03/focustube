package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** The kinds of tag in DESIGN.md, each with its colours. */
enum class TagStyle { NewVoice, Short, YourChannel, ComingLater, Watched }

/** Small uppercase label with 6 dp corners: "NEW VOICE", "SHORT", "YOUR CHANNEL", "COMING LATER", "WATCHED". */
@Composable
fun Tag(text: String, style: TagStyle, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extra = FocusTubeTheme.colors
    val (background: Color, content: Color) =
        when (style) {
            TagStyle.NewVoice -> extra.amberSoft to extra.amberTagInk
            TagStyle.Short, TagStyle.ComingLater -> scheme.surfaceVariant to scheme.onSurfaceVariant
            TagStyle.YourChannel, TagStyle.Watched -> scheme.primaryContainer to scheme.onPrimaryContainer
        }
    Text(
        text = text.uppercase(),
        style = FocusTubeTheme.typography.label.copy(fontSize = FocusTubeTheme.typography.mono.fontSize),
        fontWeight = FontWeight.SemiBold,
        color = content,
        modifier = modifier.background(
            background,
            FocusTubeTheme.shapes.tag
        ).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
