package dev.aladins.focustube.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import dev.aladins.focustube.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The real SQL of PersonaDao on an in-memory database, on the phone. */
@RunWith(AndroidJUnit4::class)
class PersonaDaoTest {
    private lateinit var database: FocusTubeDatabase
    private lateinit var dao: PersonaDao

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FocusTubeDatabase::class.java
            ).build()
        dao = database.personaDao()
    }

    @After
    fun tearDown() = database.close()

    private suspend fun insert(name: String, createdAt: Long, goal: String? = null) =
        dao.insert(PersonaEntity(name = name, goal = goal, regions = "US,GB", createdAt = createdAt))

    @Test
    fun activeAreInCreationOrder() = runTest {
        insert("General", createdAt = 300)
        insert("Guitar", createdAt = 100)
        insert("Cooking", createdAt = 200)
        assertEquals(listOf("Guitar", "Cooking", "General"), dao.observeActive().first().map { it.name })
    }

    @Test
    fun archiveMovesToArchivedNewestFirst_andRestoreMovesBack() = runTest {
        val guitar = insert("Guitar", 100)
        val cooking = insert("Cooking", 200)
        val general = insert("General", 300)

        dao.setArchivedAt(cooking, 1_000)
        dao.setArchivedAt(general, 2_000)
        assertEquals(listOf("Guitar"), dao.observeActive().first().map { it.name })
        assertEquals(listOf("General", "Cooking"), dao.observeArchived().first().map { it.name })

        dao.setArchivedAt(cooking, null)
        assertEquals(listOf(guitar, cooking), dao.observeActive().first().map { it.id })
        assertNull(dao.observe(cooking).first()?.archivedAt)
    }

    @Test
    fun updateChangesNameAndGoalOnly() = runTest {
        val id = insert("Guitar", 100, goal = "Old")
        dao.updateDetails(id, "Guitar lessons", null)
        val row = dao.observe(id).first()!!
        assertEquals("Guitar lessons", row.name)
        assertNull(row.goal)
        assertEquals("US,GB", row.regions)
        assertEquals(100L, row.createdAt)
    }

    @Test
    fun unknownIdIsNull() = runTest {
        assertNull(dao.observe(42).first())
    }
}
