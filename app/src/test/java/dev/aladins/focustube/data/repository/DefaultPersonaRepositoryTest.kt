package dev.aladins.focustube.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.data.settings.AppPreferences
import dev.aladins.focustube.domain.model.Region
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** The repository with a fake DAO and a real DataStore file. */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultPersonaRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher() + Job())
    private val dao = FakePersonaDao()
    private val clock =
        object : Clock {
            var now = Instant.fromEpochMilliseconds(1_000)

            override fun now() = now
        }
    private val repository by lazy {
        val dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            folder.newFile("prefs.preferences_pb")
        }
        DefaultPersonaRepository(dao, AppPreferences(dataStore), clock)
    }

    @After
    fun tearDown() = dataStoreScope.cancel()

    @Test
    fun noPersonasMeansNoCurrent() = runTest {
        assertNull(repository.observeCurrent().first())
    }

    @Test
    fun createStoresRegionsAndTime() = runTest {
        clock.now = Instant.fromEpochMilliseconds(5_000)
        val id = repository.create("Guitar", "Learn", Region.Defaults)
        val stored = dao.rows.value.single()
        assertEquals(id, stored.id)
        assertEquals("US,GB,DE,FR,ES,IT,NL", stored.regions)
        assertEquals(5_000L, stored.createdAt)
    }

    @Test
    fun currentFallsBackToOldestActiveWhenNothingSaved() = runTest {
        repository.create("Guitar", null, Region.Defaults)
        clock.now = Instant.fromEpochMilliseconds(2_000)
        repository.create("Cooking", null, Region.Defaults)
        assertEquals("Guitar", repository.observeCurrent().first()?.name)
    }

    @Test
    fun savedCurrentSurvivesAndArchivedCurrentFallsBack() = runTest {
        val guitar = repository.create("Guitar", null, Region.Defaults)
        clock.now = Instant.fromEpochMilliseconds(2_000)
        val cooking = repository.create("Cooking", null, Region.Defaults)

        repository.setCurrent(cooking)
        assertEquals(cooking, repository.observeCurrent().first()?.id)

        repository.archive(cooking)
        assertEquals(guitar, repository.observeCurrent().first()?.id)
        assertEquals(listOf("Cooking"), repository.observeArchived().first().map { it.name })
    }

    @Test
    fun archiveAndRestoreNeverDelete() = runTest {
        val id = repository.create("Guitar", "Learn", Region.Defaults)
        clock.now = Instant.fromEpochMilliseconds(9_000)
        repository.archive(id)
        assertEquals(9_000L, dao.rows.value.single().archivedAt)
        repository.restore(id)
        assertNull(dao.rows.value.single().archivedAt)
        assertEquals("Learn", repository.observe(id).first()?.goal)
    }
}

/** In-memory PersonaDao with the same ordering as the SQL queries. */
private class FakePersonaDao : PersonaDao {
    val rows = MutableStateFlow(emptyList<PersonaEntity>())
    private var nextId = 1L

    override fun observeActive(): Flow<List<PersonaEntity>> = rows.map { list ->
        list.filter { it.archivedAt == null }.sortedWith(compareBy({ it.createdAt }, { it.id }))
    }

    override fun observeArchived(): Flow<List<PersonaEntity>> = rows.map { list ->
        list.filter {
            it.archivedAt != null
        }.sortedWith(compareByDescending<PersonaEntity> { it.archivedAt }.thenByDescending { it.id })
    }

    override fun observe(id: Long): Flow<PersonaEntity?> = rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(persona: PersonaEntity): Long {
        val id = nextId++
        rows.update { it + persona.copy(id = id) }
        return id
    }

    override suspend fun updateDetails(id: Long, name: String, goal: String?) {
        rows.update { list -> list.map { if (it.id == id) it.copy(name = name, goal = goal) else it } }
    }

    override suspend fun setArchivedAt(id: Long, archivedAt: Long?) {
        rows.update { list -> list.map { if (it.id == id) it.copy(archivedAt = archivedAt) else it } }
    }
}
