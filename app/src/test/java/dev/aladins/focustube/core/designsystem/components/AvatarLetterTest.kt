package dev.aladins.focustube.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarLetterTest {
    @Test
    fun firstLetterUpperCased() {
        assertEquals("G", avatarLetter("guitar"))
        assertEquals("É", avatarLetter("  école"))
    }

    @Test
    fun wholeEmojiNotHalf() {
        assertEquals("🎸", avatarLetter("🎸 Guitar"))
    }

    @Test
    fun blankGivesQuestionMark() {
        assertEquals("?", avatarLetter("   "))
    }
}
