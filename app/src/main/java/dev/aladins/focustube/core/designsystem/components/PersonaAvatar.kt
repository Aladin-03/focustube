package dev.aladins.focustube.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme

/** Avatar sizes: small in the top bar, large on persona cards. */
enum class PersonaAvatarSize(val diameter: Dp, val fontSize: TextUnit) {
    Small(32.dp, 15.sp),
    Large(44.dp, 19.sp)
}

/**
 * A persona's avatar: the first letter of its name in a pineSoft circle. Decorative: the name is
 * always shown next to it, so screen readers skip it.
 */
@Composable
fun PersonaAvatar(name: String, modifier: Modifier = Modifier, size: PersonaAvatarSize = PersonaAvatarSize.Small) {
    Box(
        modifier =
            modifier
                .size(size.diameter)
                .background(MaterialTheme.colorScheme.primaryContainer, FocusTubeTheme.shapes.pill)
                .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatarLetter(name),
            style = FocusTubeTheme.typography.section.copy(fontSize = size.fontSize),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/** First character of the trimmed name, upper-cased (whole emoji or accented letter); "?" if blank. */
fun avatarLetter(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"
    val end = trimmed.offsetByCodePoints(0, 1)
    return trimmed.substring(0, end).uppercase()
}

@PreviewLightDark
@Composable
private fun PersonaAvatarPreview() {
    FocusTubeTheme {
        Row {
            PersonaAvatar("Guitar")
            PersonaAvatar("Cooking", size = PersonaAvatarSize.Large)
        }
    }
}
