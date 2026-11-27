package dev.aladins.focustube.work

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.ChannelRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.testing.FakeYouTubeApi
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** The daily uploads job on the phone (fake YouTube API). */
@HiltAndroidTest
class DailyUploadsWorkerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var personas: PersonaRepository

    @Inject lateinit var channels: ChannelRepository

    @Inject lateinit var api: FakeYouTubeApi

    @Inject lateinit var quota: QuotaTracker

    private val context: Context = ApplicationProvider.getApplicationContext()
    private var personaId = 0L

    @Before
    fun setUp() {
        runBlocking {
            hiltRule.inject()
            personaId = personas.create("Guitar", "Learn", listOf(Region("US")))
            channels.find(ChannelQuery.Handle("fred"))
            channels.setFollowers("UC_fred", setOf(personaId))
        }
    }

    private fun worker(force: Boolean) = TestListenableWorkerBuilder<DailyUploadsWorker>(context)
        .setWorkerFactory(workerFactory)
        .setInputData(workDataOf(DailyUploadsWorker.KEY_FORCE to force))
        .build()

    @Test
    fun findsNewUploads_butSkipsChannelsCheckedInTheLast20Hours() = runBlocking {
        api.addUpload("fred", "fred-new-1")
        val calls = api.playlistCalls
        assertEquals(ListenableWorker.Result.success(), worker(force = false).doWork())
        assertEquals(calls, api.playlistCalls) // followed (and checked) just now

        assertEquals(ListenableWorker.Result.success(), worker(force = true).doWork())
        assertEquals(calls + 1, api.playlistCalls)
        assertEquals("fred-new-1", channels.observeFeed(personaId, Clock.System.now() - 30.days).first().first().id)
    }

    @Test
    fun stopsQuietlyAtTheDailyLimit_andRetriesAfterANetworkError() = runBlocking {
        quota.setTodayForDebugBlocking(QuotaTracker.DAILY_LIMIT)
        val calls = api.playlistCalls
        assertEquals(ListenableWorker.Result.success(), worker(force = true).doWork())
        assertEquals(calls, api.playlistCalls)

        quota.setTodayForDebugBlocking(0)
        api.failWith = IOException("offline")
        assertEquals(ListenableWorker.Result.retry(), worker(force = true).doWork())
    }

    @Test
    fun scheduleEnqueuesOneDailyJobWithNetworkAndBatteryConstraints() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
        val workManager = WorkManager.getInstance(context)
        DailyUploadsWorker.schedule(workManager)
        DailyUploadsWorker.schedule(workManager) // again: kept, not duplicated
        val infos = workManager.getWorkInfosForUniqueWork(DailyUploadsWorker.UNIQUE_NAME).get()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
        assertEquals(NetworkType.CONNECTED, infos.single().constraints.requiredNetworkType)
        assertTrue(infos.single().constraints.requiresBatteryNotLow())
        assertTrue(infos.single().periodicityInfo!!.repeatIntervalMillis >= 24 * 60 * 60 * 1000L)
    }
}
