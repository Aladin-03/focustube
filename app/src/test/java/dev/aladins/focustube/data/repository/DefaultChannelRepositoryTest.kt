package dev.aladins.focustube.data.repository

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.testing.ChannelTestRig
import dev.aladins.focustube.testing.FakeYouTubeApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** What each channel action really costs, and that nothing is fetched twice. */
class DefaultChannelRepositoryTest {
    private var now = Instant.parse("2026-11-27T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = this@DefaultChannelRepositoryTest.now
    }
    private val api = FakeYouTubeApi(clock = clock)
    private val rig = ChannelTestRig(api = api, clock = clock)
    private val repo = rig.repository

    @Test
    fun handleLookupCostsOneUnit() = runTest {
        val found = (repo.find(ChannelQuery.Handle("fred")) as AppResult.Success).value
        assertEquals(listOf("UC_fred"), found.map { it.id })
        assertEquals(48_200L, found.single().subscriberCount)
        assertEquals(1L, api.unitsSpent)
    }

    @Test
    fun nameSearchCosts101_andTheSameWordsAgainCostNothingFor30Days() = runTest {
        val found = (repo.find(ChannelQuery.Search("Fingerstyle Fred")) as AppResult.Success).value
        assertEquals(listOf("UC_fingerstylefred", "UC_fingerstylefredtv"), found.map { it.id })
        assertEquals(101L, api.unitsSpent)

        repo.find(ChannelQuery.Search("  fingerstyle   FRED "))
        assertEquals(101L, api.unitsSpent)

        now += 31.days
        repo.find(ChannelQuery.Search("fingerstyle fred"))
        assertEquals(201L, api.unitsSpent) // ids again (100); details still saved
    }

    @Test
    fun firstFollowSavesFirstUploadsForTwoUnits_sharingWithASecondPersonaIsFree() = runTest {
        repo.find(ChannelQuery.Handle("fred"))
        repo.setFollowers("UC_fred", setOf(1L))
        assertEquals(3L, api.unitsSpent) // lookup 1 + playlist 1 + videos 1
        assertEquals(6, repo.observeUploads("UC_fred").first().size)
        assertTrue(repo.observe("UC_fred").first()!!.hasMoreUploads)

        repo.setFollowers("UC_fred", setOf(1L, 2L))
        assertEquals(3L, api.unitsSpent)
        assertEquals(setOf(1L, 2L), repo.observeFollowers("UC_fred").first())
        assertEquals(6, repo.observeFeed(2L, now - 30.days).first().size)
    }

    @Test
    fun checkCostsOneUnitPerChannel_plusOneForAllNewVideos() = runTest {
        listOf("fred", "ana").forEach {
            repo.find(ChannelQuery.Handle(it))
            repo.setFollowers("UC_$it", setOf(1L))
        }
        val before = api.unitsSpent

        assertEquals(AppResult.Success(0), repo.checkUploads(listOf("UC_fred", "UC_ana")))
        assertEquals(before + 2, api.unitsSpent) // nothing new: no videos.list

        api.addUpload("fred", "fred-new-1")
        api.addUpload("ana", "ana-new-1")
        assertEquals(AppResult.Success(2), repo.checkUploads(listOf("UC_fred", "UC_ana")))
        assertEquals(before + 2 + 3, api.unitsSpent) // 2 playlists + 1 batched videos.list
        val newest = repo.observeFeed(1L, now - 30.days).first().take(2).map { it.id }.toSet()
        assertEquals(setOf("fred-new-1", "ana-new-1"), newest)
        assertNotNull(repo.observe("UC_fred").first()!!.lastCheckedAt)
    }

    @Test
    fun moreUploadsCostsTwoUnits_untilThereAreNoMore() = runTest {
        repo.find(ChannelQuery.Handle("fred"))
        repo.setFollowers("UC_fred", setOf(1L))
        val before = api.unitsSpent
        repo.loadMoreUploads("UC_fred")
        assertEquals(before + 2, api.unitsSpent)
        assertEquals(12, repo.observeUploads("UC_fred").first().size)
        assertFalse(repo.observe("UC_fred").first()!!.hasMoreUploads)
        repo.loadMoreUploads("UC_fred")
        assertEquals(before + 2, api.unitsSpent)
    }

    @Test
    fun openingAnUnknownChannelCostsThreeUnits() = runTest {
        repo.ensureChannel("UC_lena")
        assertEquals(3L, api.unitsSpent)
        repo.ensureChannel("UC_lena")
        assertEquals(3L, api.unitsSpent)
    }

    @Test
    fun feedShowsOnlyTheLast30Days_newestFirst() = runTest {
        repo.find(ChannelQuery.Handle("fred"))
        repo.setFollowers("UC_fred", setOf(1L))
        repo.loadMoreUploads("UC_fred")
        val feed = repo.observeFeed(1L, now - 8.days).first()
        assertEquals((0..7).map { "fred-u$it" }, feed.map { it.id }) // u8 is 8 days 1 hour old
    }

    @Test
    fun unfollowThenRestoreKeepsTheDate_andCleanupKeepsFollowedChannels() = runTest {
        repo.find(ChannelQuery.Handle("fred"))
        repo.find(ChannelQuery.Handle("ana"))
        repo.setFollowers("UC_fred", setOf(1L))
        val since = repo.followedSince(1L, "UC_fred")!!
        repo.unfollow(1L, "UC_fred")
        assertNull(repo.followedSince(1L, "UC_fred"))
        repo.restoreFollow(1L, "UC_fred", since)
        assertEquals(since, repo.followedSince(1L, "UC_fred"))

        now += 31.days
        repo.removeStale()
        assertNotNull(repo.observe("UC_fred").first())
        assertNull(repo.observe("UC_ana").first()) // nobody follows it
    }
}
