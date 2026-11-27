package dev.aladins.focustube.feature.channels

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.domain.usecase.CheckUploadsUseCase
import dev.aladins.focustube.domain.usecase.GroupByDayUseCase
import dev.aladins.focustube.domain.usecase.ObserveLibraryMarksUseCase
import dev.aladins.focustube.domain.usecase.ToggleWatchLaterUseCase
import dev.aladins.focustube.navigation.Route
import dev.aladins.focustube.testing.ChannelTestRig
import dev.aladins.focustube.testing.FakeHistoryRepository
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeQuotaRepository
import dev.aladins.focustube.testing.FakeWatchLaterRepository
import dev.aladins.focustube.testing.FakeYouTubeApi
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import java.time.ZoneId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** My channels (08), Add channel (09) and the channel page (10). */
class ChannelsViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val now = Instant.parse("2026-11-27T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = this@ChannelsViewModelsTest.now
    }
    private val api = FakeYouTubeApi(clock = clock)
    private val rig = ChannelTestRig(api = api, clock = clock)
    private val personas =
        FakePersonaRepository(listOf(persona(1, "Guitar", goal = "Learn"), persona(2, "General")), currentId = 1)
    private val watchLater = FakeWatchLaterRepository()
    private val marks = ObserveLibraryMarksUseCase(watchLater, FakeHistoryRepository())
    private val quota = FakeQuotaRepository()

    private fun myChannels() = ChannelsViewModel(
        personas, rig.repository, quota, marks, ToggleWatchLaterUseCase(watchLater),
        CheckUploadsUseCase(rig.repository, clock), GroupByDayUseCase(), clock, ZoneId.of("UTC")
    )

    private suspend fun follow(slug: String, vararg personaIds: Long) {
        rig.repository.find(ChannelQuery.Handle(slug))
        rig.repository.setFollowers("UC_$slug", personaIds.toSet())
    }

    @Test
    fun feedGroupsTheCurrentPersonasUploadsByDay() = runTest {
        follow("fred", 1)
        follow("ana", 2)
        myChannels().uiState.test {
            val state = expectMostRecentItem()
            assertEquals(listOf("Fred Channel"), state.channels.map { it.title })
            assertEquals(6, state.feed.size) // u0..u5: one per day
            assertEquals(listOf("fred-u0"), state.feed.first().items.map { it.id })
            assertEquals(now, state.lastCheckedAt)
        }
    }

    @Test
    fun checkNowFindsNewUploads_andIsOffAtTheDailyLimit() = runTest {
        follow("fred", 1)
        val vm = myChannels()
        vm.uiState.test {
            api.addUpload("fred", "fred-new-1")
            vm.onCheckNow()
            assertEquals("fred-new-1", expectMostRecentItem().feed.first().items.first().id)
            quota.exhaust()
            assertFalse(expectMostRecentItem().canCheck)
        }
    }

    @Test
    fun removingAChannelFromThePersonaCanBeUndone_otherPersonasKeepIt() = runTest {
        follow("fred", 1, 2)
        val vm = myChannels()
        vm.uiState.test {
            val channel = expectMostRecentItem().channels.single()
            vm.onUnfollow(channel)
            val removed = expectMostRecentItem()
            assertTrue(removed.channels.isEmpty())
            assertEquals(setOf(2L), rig.dao.followers("UC_fred").toSet())
            vm.onUndo()
            assertEquals(1, expectMostRecentItem().channels.size)
        }
    }

    @Test
    fun addChannelPreTicksTheCurrentPersona_andShowsAddedAfterSaving() = runTest {
        val vm = AddChannelViewModel(rig.repository, personas, quota)
        vm.uiState.test {
            vm.query.setTextAndPlaceCursorAtEnd("@fred")
            vm.onSearch()
            val found = expectMostRecentItem()
            val fred = found.results.single()
            assertFalse(found.isAdded(fred))
            assertEquals(setOf(1L), found.sheetTicks(fred))

            vm.onAdd(fred)
            vm.onSave(setOf(1L, 2L))
            val saved = expectMostRecentItem()
            assertNull(saved.sheetChannel)
            assertTrue(saved.isAdded(fred))
        }
        assertEquals(3L, api.unitsSpent)
    }

    @Test
    fun channelPageSortsByViewsAndLoadsMore() = runTest {
        follow("fred", 1)
        val vm =
            ChannelViewModel(
                SavedStateHandle(mapOf(Route.Channel.CHANNEL_ID_KEY to "UC_fred")),
                rig.repository,
                personas,
                quota,
                marks,
                ToggleWatchLaterUseCase(watchLater)
            )
        vm.uiState.test {
            val first = expectMostRecentItem()
            assertEquals(listOf("Guitar"), first.followers.map { it.name })
            assertEquals("fred-u0", first.uploads.first().id)
            assertTrue(first.canLoadMore)

            vm.onLoadMore()
            assertEquals(12, expectMostRecentItem().uploads.size)
            vm.onSort(UploadSort.Popular)
            val popular = expectMostRecentItem()
            assertEquals(popular.uploads.sortedByDescending { it.viewCount }, popular.uploads)
            assertFalse(popular.canLoadMore)

            vm.onSaveFollowers(setOf(2L))
            assertEquals(listOf("General"), expectMostRecentItem().followers.map { it.name })
        }
    }
}
