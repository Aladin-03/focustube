package dev.aladins.focustube.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.data.local.entity.SearchCacheEntity
import dev.aladins.focustube.data.local.entity.SearchResultEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The real SQL of SearchDao, VideoDao and QuotaDao on the phone. */
@RunWith(AndroidJUnit4::class)
class SearchDaosTest {
    private lateinit var db: FocusTubeDatabase
    private var personaId = 0L

    @Before
    fun setUp() = runTest {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FocusTubeDatabase::class.java
            ).build()
        personaId = db.personaDao().insert(PersonaEntity(name = "Guitar", goal = null, regions = "US", createdAt = 0))
    }

    @After
    fun tearDown() = db.close()

    private fun video(id: String, fetchedAt: Long = 1_000) =
        VideoEntity(id, "Video $id", "c1", "Fred", 664, 0, 10, false, "", null, fetchedAt)

    private fun cache(query: String = "guitar", createdAt: Long = 1_000, lastUsedAt: Long = createdAt) =
        SearchCacheEntity(
            personaId = personaId,
            query = query,
            queryNormalized = query,
            mode = "CLASSIC",
            regions = "US",
            regionOrder = "US",
            pagesLoaded = 1,
            pageTokens = "{}",
            createdAt = createdAt,
            lastUsedAt = lastUsedAt
        )

    @Test
    fun resultsComeBackInRankOrder_andReplaceSwapsThem() = runTest {
        val dao = db.searchDao()
        db.videoDao().upsertAll(listOf(video("a"), video("b"), video("c")))
        val id = dao.insertCache(cache())
        dao.insertResults(listOf(SearchResultEntity(id, "b", 1), SearchResultEntity(id, "a", 0)))
        assertEquals(listOf("a", "b"), dao.observeResultVideos(id).first().map { it.youtubeId })

        dao.replaceResults(id, listOf(SearchResultEntity(id, "c", 0)))
        assertEquals(listOf("c"), dao.observeResultVideos(id).first().map { it.youtubeId })
        assertEquals(0, dao.maxRank(id))
    }

    @Test
    fun cacheIsFoundByItsKey_andDeletingItRemovesItsResults() = runTest {
        val dao = db.searchDao()
        db.videoDao().upsertAll(listOf(video("a")))
        val id = dao.insertCache(cache())
        dao.insertResults(listOf(SearchResultEntity(id, "a", 0)))
        assertNotNull(dao.getCache(personaId, "guitar", "CLASSIC", "US"))
        assertNull(dao.getCache(personaId, "guitar", "CLASSIC", "GB"))

        dao.deleteCache(id)
        assertEquals(emptyList<String>(), dao.resultVideoIds(id))
    }

    @Test
    fun recentIsMostRecentlyUsedFirst() = runTest {
        val dao = db.searchDao()
        dao.insertCache(cache("old", createdAt = 1, lastUsedAt = 1))
        val newer = dao.insertCache(cache("newer", createdAt = 2, lastUsedAt = 2))
        dao.insertCache(cache("newest", createdAt = 3, lastUsedAt = 3))
        dao.touch(newer, 10)
        assertEquals(
            listOf("newer", "newest", "old"),
            dao.observeRecent(personaId, "CLASSIC", 10).first().map {
                it.query
            }
        )
    }

    @Test
    fun cleanupDropsOldSearches_andOnlyUnusedOldVideos() = runTest {
        val search = db.searchDao()
        val videos = db.videoDao()
        videos.upsertAll(
            listOf(video("kept", fetchedAt = 1), video("dropped", fetchedAt = 1), video("fresh", fetchedAt = 5_000))
        )
        val keep = search.insertCache(cache("recent", createdAt = 5_000))
        search.insertResults(listOf(SearchResultEntity(keep, "kept", 0)))
        search.insertCache(cache("stale", createdAt = 1))

        assertEquals(1, search.deleteOlderThan(cutoff = 1_000))
        assertEquals(1, videos.deleteUnusedOlderThan(cutoff = 1_000))
        assertNotNull(videos.get("kept"))
        assertNull(videos.get("dropped"))
        assertNotNull(videos.get("fresh"))
    }

    @Test
    fun quotaAddsUpPerDay_andRaiseNeverLowers() = runTest {
        val dao = db.quotaDao()
        dao.addBlocking("2026-11-27", 100)
        dao.addBlocking("2026-11-27", 1)
        dao.addBlocking("2026-11-28", 5)
        assertEquals(101L, dao.observeUnits("2026-11-27").first())
        dao.raiseToAtLeastBlocking("2026-11-27", 50)
        assertEquals(101L, dao.unitsBlocking("2026-11-27"))
        dao.raiseToAtLeastBlocking("2026-11-27", 10_000)
        assertEquals(10_000L, dao.unitsBlocking("2026-11-27"))
        assertEquals(0L, dao.unitsBlocking("2026-11-29"))
    }
}
