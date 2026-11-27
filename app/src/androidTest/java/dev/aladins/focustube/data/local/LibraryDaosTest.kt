package dev.aladins.focustube.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import dev.aladins.focustube.data.local.entity.HistoryEntity
import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import dev.aladins.focustube.data.local.entity.WatchLaterEntity
import dev.aladins.focustube.data.repository.DefaultHistoryRepository
import dev.aladins.focustube.domain.model.WatchProgress
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The real SQL of WatchLaterDao, HistoryDao and the video cleanup, on the phone. */
@RunWith(AndroidJUnit4::class)
class LibraryDaosTest {
    private lateinit var db: FocusTubeDatabase
    private var guitar = 0L
    private var general = 0L

    @Before
    fun setUp() = runTest {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FocusTubeDatabase::class.java
            ).build()
        guitar = db.personaDao().insert(PersonaEntity(name = "Guitar", goal = "Learn", regions = "US", createdAt = 1))
        general = db.personaDao().insert(PersonaEntity(name = "General", goal = null, regions = "US", createdAt = 2))
        db.videoDao().upsertAll(listOf("a", "b", "c").map { video(it) })
    }

    @After
    fun tearDown() = db.close()

    private fun video(id: String, fetchedAt: Long = 1_000) =
        VideoEntity(id, "Video $id", "c1", "Fred", 664, 0, 10, false, "", null, fetchedAt)

    @Test
    fun watchLaterIsPerPersona_newestFirst_andSavingTwiceKeepsTheDate() = runTest {
        val dao = db.watchLaterDao()
        dao.insert(WatchLaterEntity(guitar, "a", 10))
        dao.insert(WatchLaterEntity(guitar, "b", 20))
        dao.insert(WatchLaterEntity(general, "c", 30))
        dao.insert(WatchLaterEntity(guitar, "a", 99)) // again: ignored
        assertEquals(listOf("b", "a"), dao.observe(guitar).first().map { it.video.youtubeId })
        assertEquals(10L, dao.observe(guitar).first().last().addedAt)
        assertEquals(listOf("c"), dao.observe(general).first().map { it.video.youtubeId })
    }

    @Test
    fun watchLaterRowsCarryHistoryProgress() = runTest {
        db.watchLaterDao().insert(WatchLaterEntity(guitar, "a", 10))
        db.historyDao().upsert(HistoryEntity(guitar, "a", 50, 410, 664, false, null))
        val row = db.watchLaterDao().observe(guitar).first().single()
        assertEquals(410L, row.positionSec)
        assertEquals(664L, row.durationSec)
        assertEquals(664L, row.video.durationSec)
    }

    @Test
    fun historyIsPerPersona_newestFirst_watchedStaysWatched_andClearOnlyTouchesOnePersona() = runTest {
        val repo = DefaultHistoryRepository(db.historyDao())
        repo.save(guitar, "a", WatchProgress(400, 664, true), Instant.fromEpochMilliseconds(100))
        repo.save(guitar, "b", WatchProgress(50, 664, false), Instant.fromEpochMilliseconds(200))
        repo.save(general, "c", WatchProgress(50, 664, false), Instant.fromEpochMilliseconds(300))
        repo.save(guitar, "a", WatchProgress(10, 664, false), Instant.fromEpochMilliseconds(400)) // re-watch

        assertEquals(listOf("a", "b"), repo.observe(guitar).first().map { it.video.id })
        val a = db.historyDao().get(guitar, "a")!!
        assertTrue(a.isWatched)
        assertEquals(100L, a.watchedAt)
        assertEquals(setOf("a"), repo.observeWatchedIds(guitar).first())

        repo.clear(guitar)
        assertTrue(repo.observe(guitar).first().isEmpty())
        assertEquals(1, repo.observe(general).first().size)
    }

    @Test
    fun cleanupNeverDropsLibraryVideos_evenOfArchivedPersonas() = runTest {
        db.videoDao().upsertAll(listOf(video("saved"), video("watched"), video("unused")))
        db.watchLaterDao().insert(WatchLaterEntity(general, "saved", 1))
        db.historyDao().upsert(HistoryEntity(general, "watched", 1, 10, 664, false, null))
        db.personaDao().setArchivedAt(general, 5)

        db.videoDao().deleteUnusedOlderThan(cutoff = 2_000)
        assertNotNull(db.videoDao().get("saved"))
        assertNotNull(db.videoDao().get("watched"))
        assertNull(db.videoDao().get("unused"))
        assertEquals(listOf("saved", "watched"), db.videoDao().keptIdsFetchedBefore(2_000).sorted())
    }
}
