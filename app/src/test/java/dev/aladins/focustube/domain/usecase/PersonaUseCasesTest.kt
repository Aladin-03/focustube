package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.domain.model.PersonaNameError
import dev.aladins.focustube.domain.model.PersonaSaveResult
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.persona
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Create, update, archive and restore rules. */
class PersonaUseCasesTest {
    private val validate = ValidatePersonaInputUseCase()

    @Test
    fun createTrimsUsesDefaultRegionsAndBecomesCurrent() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "General")), currentId = 1)
        val result = CreatePersonaUseCase(repo, validate)("  Guitar ", "  Learn fingerstyle.  ")

        val id = (result as PersonaSaveResult.Saved).personaId
        val created = repo.observe(id).first()!!
        assertEquals("Guitar", created.name)
        assertEquals("Learn fingerstyle.", created.goal)
        assertEquals(Region.Defaults, created.regions)
        assertEquals(id, repo.observeCurrent().first()?.id)
    }

    @Test
    fun createWithBlankGoalHasNoGoal() = runTest {
        val repo = FakePersonaRepository()
        val id = (CreatePersonaUseCase(repo, validate)("General", "   ") as PersonaSaveResult.Saved).personaId
        assertNull(repo.observe(id).first()!!.goal)
    }

    @Test
    fun createRefusesDuplicateActiveName() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar")))
        val result = CreatePersonaUseCase(repo, validate)("GUITAR", "")
        assertEquals(PersonaNameError.Taken, (result as PersonaSaveResult.Invalid).errors.name)
        assertEquals(1, repo.all.size)
    }

    @Test
    fun createAllowsNameOfArchivedPersona() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "General"), persona(2, "Guitar", archivedMinute = 9)))
        assertTrue(CreatePersonaUseCase(repo, validate)("Guitar", "") is PersonaSaveResult.Saved)
    }

    @Test
    fun updateKeepsOwnNameAndRefusesAnothersName() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "Cooking")))
        val update = UpdatePersonaUseCase(repo, validate)

        assertTrue(update(1, "guitar", "New goal") is PersonaSaveResult.Saved)
        assertEquals("guitar", repo.observe(1).first()!!.name)
        assertEquals("New goal", repo.observe(1).first()!!.goal)

        val refused = update(1, "Cooking", "")
        assertEquals(PersonaNameError.Taken, (refused as PersonaSaveResult.Invalid).errors.name)
    }

    @Test
    fun archiveKeepsCurrentWhenAnotherPersonaIsArchived() = runTest {
        val repo = FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "Cooking")), currentId = 1)
        assertEquals(ArchivePersonaUseCase.Result.Archived, ArchivePersonaUseCase(repo)(2))
        assertEquals(1L, repo.observeCurrent().first()?.id)
        assertTrue(repo.observe(2).first()!!.isArchived)
    }

    @Test
    fun archivingCurrentSwitchesToOldestRemaining() = runTest {
        val repo =
            FakePersonaRepository(
                listOf(persona(1, "Guitar"), persona(2, "Cooking"), persona(3, "General")),
                currentId = 2
            )
        ArchivePersonaUseCase(repo)(2)
        assertEquals(1L, repo.savedCurrentId)

        ArchivePersonaUseCase(repo)(1)
        assertEquals(3L, repo.savedCurrentId)
    }

    @Test
    fun lastActivePersonaCannotBeArchived() = runTest {
        val repo =
            FakePersonaRepository(listOf(persona(1, "Guitar"), persona(2, "Old", archivedMinute = 5)), currentId = 1)
        assertEquals(ArchivePersonaUseCase.Result.LastActivePersona, ArchivePersonaUseCase(repo)(1))
        assertTrue(!repo.observe(1).first()!!.isArchived)
    }

    @Test
    fun restoreKeepsCurrentPersona() = runTest {
        val repo =
            FakePersonaRepository(
                listOf(persona(1, "Guitar"), persona(2, "Cooking", archivedMinute = 9)),
                currentId = 1
            )
        RestorePersonaUseCase(repo)(2)
        assertTrue(!repo.observe(2).first()!!.isArchived)
        assertEquals(1L, repo.observeCurrent().first()?.id)
    }

    @Test
    fun restoreRenamesWhenAnActivePersonaHasTheSameName() = runTest {
        val repo =
            FakePersonaRepository(
                listOf(persona(1, "Guitar", archivedMinute = 9), persona(2, "guitar"), persona(3, "Guitar 2"))
            )
        RestorePersonaUseCase(repo)(1)
        assertEquals("Guitar 3", repo.observe(1).first()!!.name)
    }

    @Test
    fun restoredLongNameStaysWithinLimit() = runTest {
        val long = "a".repeat(30)
        val repo = FakePersonaRepository(listOf(persona(1, long, archivedMinute = 9), persona(2, long)))
        RestorePersonaUseCase(repo)(1)
        val name = repo.observe(1).first()!!.name
        assertEquals("a".repeat(28) + " 2", name)
    }
}
