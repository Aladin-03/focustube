package dev.aladins.focustube.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.repository.VideoRepository
import dev.aladins.focustube.domain.usecase.CheckUploadsUseCase
import java.util.concurrent.TimeUnit

/**
 * Once a day: checks channels followed by active personas for new uploads (skipping any checked
 * in the last 20 hours) and refreshes kept video details older than 30 days. Needs a network
 * connection and a battery that isn't low. Stops quietly at the daily limit (tomorrow's run
 * continues) and retries later after a network error.
 */
@HiltWorker
class DailyUploadsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val checkUploads: CheckUploadsUseCase,
    private val videos: VideoRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val force = inputData.getBoolean(KEY_FORCE, false)
        return when (val result = checkUploads(personaId = null, force = force)) {
            is AppResult.Success -> {
                videos.refreshStaleSavedVideos()
                Result.success()
            }

            is AppResult.Failure ->
                when (result.error) {
                    AppError.Network -> Result.retry()

                    // Daily limit, key problems, YouTube errors: try again on tomorrow's run.
                    else -> Result.success()
                }
        }
    }

    companion object {
        const val UNIQUE_NAME = "daily-uploads"

        /** Input flag (debug trigger only): check every channel, ignoring the 20-hour gap. */
        const val KEY_FORCE = "force"

        /** Network required, not on low battery. */
        val constraints: Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

        /** Schedules the daily run once; calling it again keeps the existing schedule. */
        fun schedule(workManager: WorkManager) {
            val request =
                PeriodicWorkRequestBuilder<DailyUploadsWorker>(24, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                    .build()
            workManager.enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
