package dev.aladins.focustube.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dev.aladins.focustube.data.mapper.toEntity
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.data.settings.AppPreferences
import dev.aladins.focustube.testing.FakeVideoDao
import dev.aladins.focustube.testing.FakeYouTubeApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Library details older than 30 days are refreshed in one batch, at most once a day. */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryRefreshTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(UnconfinedTestDispatcher() + Job())
    private var now = Instant.parse("2026-11-27T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = this@LibraryRefreshTest.now
    }
    private val api = FakeYouTubeApi()
    private val dao = FakeVideoDao()
    private val repository by lazy {
        val prefs =
            AppPreferences(PreferenceDataStoreFactory.create(scope = scope) { folder.newFile("p.preferences_pb") })
        DefaultVideoRepository(dao, YouTubeDataSource(api, Json { ignoreUnknownKeys = true }), clock, prefs)
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun staleLibraryVideosRefreshOncePerDay_freshOnesAndNonLibraryOnesNever() = runTest {
        val old = now - 31.days
        dao.upsertAll(
            listOf(
                FakeYouTubeApi.videoDto("lib-old").toEntity(old),
                FakeYouTubeApi.videoDto("lib-new").toEntity(now - 2.days),
                FakeYouTubeApi.videoDto("search-only-old").toEntity(old)
            )
        )
        dao.libraryIds = setOf("lib-old", "lib-new")

        repository.refreshStaleSavedVideos()
        assertEquals(1, api.videosCalls)
        assertEquals(now.toEpochMilliseconds(), dao.get("lib-old")!!.fetchedAt)
        assertEquals(old.toEpochMilliseconds(), dao.get("search-only-old")!!.fetchedAt)

        now += 2.hours
        dao.upsertAll(listOf(FakeYouTubeApi.videoDto("lib-old").toEntity(old)))
        repository.refreshStaleSavedVideos()
        assertEquals(1, api.videosCalls) // same day: skipped

        now += 1.days
        repository.refreshStaleSavedVideos()
        assertEquals(2, api.videosCalls)
    }
}
