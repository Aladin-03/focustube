package dev.aladins.focustube.data.repository

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import dev.aladins.focustube.testing.DirectTransactionRunner
import dev.aladins.focustube.testing.FakeSearchDao
import dev.aladins.focustube.testing.FakeVideoDao
import dev.aladins.focustube.testing.FakeYouTubeApi
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cache-first search: the YouTube calls (and units) each action really makes. */
class DefaultSearchRepositoryTest {
    private val api = FakeYouTubeApi()
    private val videoDao = FakeVideoDao()
    private val searchDao = FakeSearchDao(videoDao)
    private var now = Instant.parse("2026-11-27T12:00:00Z")
    private val json = Json { ignoreUnknownKeys = true }
    private val repository =
        DefaultSearchRepository(
            transaction = DirectTransactionRunner,
            searchDao = searchDao,
            videoDao = videoDao,
            youTube = YouTubeDataSource(api, json),
            json = json,
            clock = object : Clock {
                override fun now() = this@DefaultSearchRepositoryTest.now
            }
        )

    private val usGbDe = listOf("US", "GB", "DE").map(::Region)

    private fun key(query: String = "fingerstyle basics", persona: Long = 1, regions: List<Region> = usGbDe) =
        SearchKey(persona, query, SearchMode.Classic, regions)

    @Test
    fun firstSearchCosts101Units_andSavesResultsInOrder() = runTest {
        assertTrue(repository.search(key(), refresh = false) is AppResult.Success)
        assertEquals(101L, api.unitsSpent)
        assertEquals(listOf("US"), api.searchedRegions)

        val saved = repository.observe(key()).first()!!
        assertEquals(6, saved.videos.size)
        assertEquals("fingerstyle-basics-US-p1-0", saved.videos.first().id)
        assertTrue(saved.hasMore)
    }

    @Test
    fun sameSearchAgainCostsZeroUnits_evenWithOtherSpacingCaseAndRegionOrder() = runTest {
        repository.search(key(), refresh = false)
        val before = api.unitsSpent

        repository.search(key(), refresh = false)
        repository.search(key(query = "  Fingerstyle   BASICS "), refresh = false)
        repository.search(key(regions = usGbDe.reversed()), refresh = false)

        assertEquals(before, api.unitsSpent)
        assertEquals(1, api.searchCalls)
    }

    @Test
    fun refreshCostsAnother101() = runTest {
        repository.search(key(), refresh = false)
        repository.search(key(), refresh = true)
        assertEquals(202L, api.unitsSpent)
    }

    @Test
    fun otherPersonaOrRegionsIsANewSearch() = runTest {
        repository.search(key(), refresh = false)
        repository.search(key(persona = 2), refresh = false)
        repository.search(key(regions = listOf(Region("NL"))), refresh = false)
        assertEquals(3, api.searchCalls)
    }

    @Test
    fun moreResultsTakeRegionsInTurn_thenTheirNextPages() = runTest {
        repository.search(key(), refresh = false)
        repeat(5) { repository.loadMore(key()) }
        // Page 1 per region in the persona's order, then page 2 for each (2 pages per region in the fake).
        assertEquals(listOf("US", "GB", "DE", "US", "GB", "DE"), api.searchedRegions)
        val saved = repository.observe(key()).first()!!
        assertEquals(36, saved.videos.size)
        assertEquals(36, saved.videos.map { it.id }.distinct().size)
        assertFalse(saved.hasMore)

        val callsBefore = api.searchCalls
        repository.loadMore(key())
        assertEquals(callsBefore, api.searchCalls)
    }

    @Test
    fun failedFetchKeepsSavedResults() = runTest {
        repository.search(key(), refresh = false)
        api.failWith = IOException("offline")
        assertEquals(AppResult.Failure(AppError.Network), repository.search(key(), refresh = true))
        assertEquals(6, repository.observe(key()).first()!!.videos.size)
    }

    @Test
    fun savesOlderThan30DaysAreFetchedAgain_andCleanedUp() = runTest {
        repository.search(key(), refresh = false)
        now += 31.days
        assertNull(repository.observe(key()).first())
        repository.search(key(), refresh = false)
        assertEquals(2, api.searchCalls)

        now += 31.days
        repository.removeStale()
        assertTrue(searchDao.caches.value.isEmpty())
        assertTrue(videoDao.rows.value.isEmpty())
    }

    @Test
    fun recentListsSavedSearchesNewestFirst_andRemoveDeletesOne() = runTest {
        repository.search(key(query = "fingerstyle basics"), refresh = false)
        now += 1.days
        repository.search(key(query = "travis picking"), refresh = false)
        assertEquals(
            listOf("travis picking", "fingerstyle basics"),
            repository.observeRecent(1, SearchMode.Classic).first().map { it.query }
        )
        repository.remove(key(query = "travis picking"))
        assertEquals(
            listOf("fingerstyle basics"),
            repository.observeRecent(1, SearchMode.Classic).first().map {
                it.query
            }
        )
    }
}
