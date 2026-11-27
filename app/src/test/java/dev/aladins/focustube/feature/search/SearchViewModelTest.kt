package dev.aladins.focustube.feature.search

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.turbine.test
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import dev.aladins.focustube.domain.model.ShortsFilter
import dev.aladins.focustube.domain.model.WatchProgress
import dev.aladins.focustube.domain.usecase.ObserveLibraryMarksUseCase
import dev.aladins.focustube.domain.usecase.ToggleWatchLaterUseCase
import dev.aladins.focustube.testing.FakeHistoryRepository
import dev.aladins.focustube.testing.FakePersonaRepository
import dev.aladins.focustube.testing.FakeQuotaRepository
import dev.aladins.focustube.testing.FakeSearchRepository
import dev.aladins.focustube.testing.FakeSettingsRepository
import dev.aladins.focustube.testing.FakeWatchLaterRepository
import dev.aladins.focustube.testing.MainDispatcherRule
import dev.aladins.focustube.testing.persona
import dev.aladins.focustube.testing.video
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val personas = FakePersonaRepository(listOf(persona(1, "Guitar", goal = "Learn")), currentId = 1)
    private val searches = FakeSearchRepository().apply {
        results =
            listOf(video("v1"), video("s1", isShort = true), video("v2"))
    }
    private val quota = FakeQuotaRepository()
    private val watchLater = FakeWatchLaterRepository()
    private val history = FakeHistoryRepository()
    private val viewModel by lazy {
        SearchViewModel(
            personas,
            searches,
            quota,
            FakeSettingsRepository(),
            ObserveLibraryMarksUseCase(watchLater, history),
            ToggleWatchLaterUseCase(watchLater)
        )
    }

    private fun type(text: String) {
        viewModel.query.setTextAndPlaceCursorAtEnd(text)
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun searchUsesPersonaRegions_andShowsSavedResults() = runTest {
        viewModel.uiState.test {
            type("fingerstyle basics")
            viewModel.onSearch()
            val state = expectMostRecentItem()
            assertEquals("fingerstyle basics", state.submittedQuery)
            assertEquals(3, state.visibleVideos.size)
            assertEquals(Region.Defaults, searches.searchCalls.single().first.regions)
            assertFalse(searches.searchCalls.single().second)
        }
    }

    @Test
    fun filterNeverCallsYouTube() = runTest {
        viewModel.uiState.test {
            type("fingerstyle basics")
            viewModel.onSearch()
            viewModel.onFilterChange(ShortsFilter.Shorts)
            assertEquals(listOf("s1"), expectMostRecentItem().visibleVideos.map { it.id })
            viewModel.onFilterChange(ShortsFilter.Videos)
            assertEquals(listOf("v1", "v2"), expectMostRecentItem().visibleVideos.map { it.id })
        }
        assertEquals(1, searches.searchCalls.size)
    }

    @Test
    fun dailyLimitShowsBanner_keepsSavedResults_andDisablesRefreshAndMore() = runTest {
        viewModel.uiState.test {
            type("fingerstyle basics")
            viewModel.onSearch()
            quota.exhaust()
            val state = expectMostRecentItem()
            assertTrue(state.isLimitReached)
            assertEquals(3, state.visibleVideos.size)
            assertFalse(state.canRefresh)
            assertFalse(state.canLoadMore)
        }
    }

    @Test
    fun offlineWithNothingSavedShowsNetworkError() = runTest {
        searches.nextResult = AppResult.Failure(AppError.Network)
        viewModel.uiState.test {
            type("travis picking")
            viewModel.onSearch()
            val state = expectMostRecentItem()
            assertEquals(AppError.Network, state.error)
            assertNull(state.saved)
        }
    }

    @Test
    fun refreshAndMoreResultsAskTheRepository() = runTest {
        type("fingerstyle basics")
        viewModel.onSearch()
        viewModel.onRefresh()
        viewModel.onLoadMore()
        assertTrue(searches.searchCalls.last().second)
        assertEquals(1, searches.loadMoreCalls.size)
    }

    @Test
    fun applyingRegionsSavesThemOnThePersona_andRerunsTheOpenSearch() = runTest {
        type("fingerstyle basics")
        viewModel.onSearch()
        val nl = listOf(Region("NL"))
        viewModel.onRegionsApply(nl)
        assertEquals(nl, personas.observe(1).first()!!.regions)
        assertEquals(nl, searches.searchCalls.last().first.regions)
    }

    @Test
    fun recentShowsOnlySearchesWithTheCurrentRegions_andOpensThemForFree() = runTest {
        val mine = RecentSearch("fingerstyle basics", Region.Defaults, Instant.fromEpochMilliseconds(2))
        val other = RecentSearch("old search", listOf(Region("NL")), Instant.fromEpochMilliseconds(1))
        searches.recent.value = listOf(mine, other)
        searches.save(SearchKey(1, mine.query, SearchMode.Classic, mine.regions), listOf(video("v1")))
        viewModel.uiState.test {
            assertEquals(listOf("fingerstyle basics"), expectMostRecentItem().recent.map { it.query })
            viewModel.onRecentClick(mine)
            assertEquals(listOf("v1"), expectMostRecentItem().visibleVideos.map { it.id })
        }
        assertEquals("fingerstyle basics", viewModel.query.text.toString())
    }

    @Test
    fun clearingTheFieldGoesBackToRecent() = runTest {
        viewModel.uiState.test {
            type("fingerstyle basics")
            viewModel.onSearch()
            assertEquals("fingerstyle basics", expectMostRecentItem().submittedQuery)
            type("")
            assertNull(expectMostRecentItem().submittedQuery)
        }
    }

    @Test
    fun bookmarkTogglesWatchLater_andRowsGetProgress() = runTest {
        history.save(1, "v2", WatchProgress(100, 664, false), Instant.fromEpochMilliseconds(5))
        viewModel.uiState.test {
            type("fingerstyle basics")
            viewModel.onSearch()
            viewModel.onToggleWatchLater("v1")
            val state = expectMostRecentItem()
            assertEquals(setOf("v1"), state.marks.savedIds)
            assertEquals(100L, state.marks.progress["v2"]?.positionSeconds)
            viewModel.onToggleWatchLater("v1")
            assertTrue(expectMostRecentItem().marks.savedIds.isEmpty())
        }
    }
}
