package dev.aladins.focustube.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.aladins.focustube.work.DailyUploadsWorker

/**
 * Debug builds only (not in the release APK): run the daily uploads check now, from adb.
 * Only callers with the DUMP permission (the adb shell) may send it.
 *
 *   adb shell am broadcast -n dev.aladins.focustube/.debug.DebugWorkReceiver --es work uploads
 *   (add --ez force true to also check channels checked in the last 20 hours)
 */
class DebugWorkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getStringExtra("work") != "uploads") {
            Log.w(TAG, "Unknown work: use --es work uploads")
            return
        }
        val force = intent.getBooleanExtra("force", false)
        val request =
            OneTimeWorkRequestBuilder<DailyUploadsWorker>()
                .setConstraints(DailyUploadsWorker.constraints)
                .setInputData(workDataOf(DailyUploadsWorker.KEY_FORCE to force))
                .build()
        WorkManager.getInstance(context).enqueue(request)
        Log.i(TAG, "Daily uploads check enqueued (force=$force)")
    }

    private companion object {
        const val TAG = "FocusTubeDebug"
    }
}
