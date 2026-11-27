package dev.aladins.focustube.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug builds only (not in the release APK): simulate the YouTube daily limit for testing.
 *
 *   adb shell am broadcast -n dev.aladins.focustube/.debug.DebugQuotaReceiver --es state exhausted
 *   adb shell am broadcast -n dev.aladins.focustube/.debug.DebugQuotaReceiver --es state reset
 *   adb shell am broadcast -n dev.aladins.focustube/.debug.DebugQuotaReceiver --es state reset --el units 505
 */
@AndroidEntryPoint
class DebugQuotaReceiver : BroadcastReceiver() {
    @Inject lateinit var tracker: QuotaTracker

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra("state")
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (state) {
                    "exhausted" -> tracker.setTodayForDebugBlocking(QuotaTracker.DAILY_LIMIT)
                    "reset" -> tracker.setTodayForDebugBlocking(intent.getLongExtra("units", 0))
                    else -> Log.w(TAG, "Unknown state: $state (use exhausted or reset)")
                }
                Log.i(TAG, "Quota state set: $state")
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "FocusTubeDebug"
    }
}
