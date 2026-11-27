package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.PersonaGoalError
import dev.aladins.focustube.domain.model.PersonaNameError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePersonaInputUseCaseTest {
    private val validate = ValidatePersonaInputUseCase()

    @Test
    fun validNameAndEmptyGoal() {
        assertTrue(validate("Guitar", "", emptyList()).isValid)
    }

    @Test
    fun blankNameIsEmpty() {
        assertEquals(PersonaNameError.Empty, validate("   ", "", emptyList()).name)
    }

    @Test
    fun nameLimitIs30AfterTrimming() {
        assertNull(validate("  " + "a".repeat(30) + "  ", "", emptyList()).name)
        assertEquals(PersonaNameError.TooLong, validate("a".repeat(31), "", emptyList()).name)
    }

    @Test
    fun goalLimitIs300AfterTrimming() {
        assertNull(validate("Guitar", " " + "g".repeat(300) + " ", emptyList()).goal)
        assertEquals(PersonaGoalError.TooLong, validate("Guitar", "g".repeat(301), emptyList()).goal)
    }

    @Test
    fun duplicateNameIgnoresCaseAndSpaces() {
        assertEquals(PersonaNameError.Taken, validate(" guitar ", "", listOf("Guitar")).name)
        assertNull(validate("Guitar 2", "", listOf("Guitar")).name)
    }
}
