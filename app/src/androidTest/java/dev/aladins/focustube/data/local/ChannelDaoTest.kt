package dev.aladins.focustube.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import dev.aladins.focustube.data.local.entity.ChannelEntity
import dev.aladins.focustube.data.local.entity.ChannelUploadEntity
import dev.aladins.focustube.data.local.entity.PersonaChannelEntity
import dev.aladins.focustube.data.local.entity.PersonaEntity
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

/** The real SQL of ChannelDao and the video cleanup for channel uploads, on the phone. */
@RunWith(AndroidJUnit4::class)
class ChannelDaoTest {
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
        db.channelDao().upsertDetails(listOf(channel("fred"), channel("ana")))
        db.videoDao().upsertAll((1..4).map { video("v$it") })
        db.channelDao().insertUploads(
            listOf(
                ChannelUploadEntity("UC_fred", "v1", 100),
                ChannelUploadEntity("UC_fred", "v2", 300),
                ChannelUploadEntity("UC_ana", "v3", 200),
                ChannelUploadEntity("UC_ana", "v4", 50)
            )
        )
    }

    @After
    fun tearDown() = db.close()

    private fun channel(slug: String) = ChannelEntity(
        "UC_$slug",
        "${slug.uppercase()} channel",
        "@$slug",
        null,
        1000,
        10,
        "UU_$slug",
        fetchedAt = 1_000
    )

    private fun video(id: String) = VideoEntity(id, "Video $id", "c", "C", 600, 0, 10, false, "", null, 1_000)

    @Test
    fun aChannelCanBelongToSeveralPersonas_andTheFeedIsPerPersonaNewestFirst() = runTest {
        val dao = db.channelDao()
        dao.follow(PersonaChannelEntity(guitar, "UC_fred", 1))
        dao.follow(PersonaChannelEntity(general, "UC_fred", 1))
        dao.follow(PersonaChannelEntity(general, "UC_ana", 1))

        assertEquals(setOf(guitar, general), dao.observeFollowers("UC_fred").first().toSet())
        assertEquals(listOf("v2", "v1"), dao.observeFeed(guitar, since = 0).first().map { it.youtubeId })
        assertEquals(listOf("v2", "v3", "v1"), dao.observeFeed(general, since = 60).first().map { it.youtubeId })
        val fred = dao.observeFollowed(guitar).first().single()
        assertEquals(300L, fred.latestUploadAt)
    }

    @Test
    fun detailsUpdateKeepsPagingAndCheckTimes() = runTest {
        val dao = db.channelDao()
        dao.setNextToken("UC_fred", "page-2")
        dao.setLastChecked(listOf("UC_fred"), 42)
        dao.upsertDetails(listOf(channel("fred").copy(title = "Renamed", subscriberCount = 2000, fetchedAt = 5_000)))
        val fred = dao.get("UC_fred")!!
        assertEquals("Renamed", fred.title)
        assertEquals("page-2", fred.uploadsNextToken)
        assertEquals(42L, fred.lastCheckedAt)
    }

    @Test
    fun activePersonasOnly_forTheDailyCheck() = runTest {
        val dao = db.channelDao()
        dao.follow(PersonaChannelEntity(guitar, "UC_fred", 1))
        dao.follow(PersonaChannelEntity(general, "UC_ana", 1))
        db.personaDao().setArchivedAt(general, 9)
        assertEquals(listOf("UC_fred"), dao.followedIdsOfActivePersonas())
    }

    @Test
    fun cleanupKeepsFollowedChannelsAndTheirVideos_evenOfArchivedPersonas() = runTest {
        val dao = db.channelDao()
        dao.follow(PersonaChannelEntity(general, "UC_fred", 1))
        db.personaDao().setArchivedAt(general, 9)

        assertEquals(1, dao.deleteUnfollowedOlderThan(cutoff = 2_000))
        assertNotNull(dao.get("UC_fred"))
        assertNull(dao.get("UC_ana"))
        assertEquals(emptyList<String>(), dao.uploadIds("UC_ana")) // cascade

        db.videoDao().deleteUnusedOlderThan(cutoff = 2_000)
        assertNotNull(db.videoDao().get("v1"))
        assertNull(db.videoDao().get("v3"))
        assertEquals(listOf("v1", "v2"), db.videoDao().keptIdsFetchedBefore(2_000).sorted())
    }
}
