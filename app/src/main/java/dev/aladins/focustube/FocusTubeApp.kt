package dev.aladins.focustube

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/** The FocusTube application: the root of Hilt's dependency graph. */
@HiltAndroidApp
class FocusTubeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG && BuildConfig.YOUTUBE_API_KEY.isBlank()) {
            // Never log the key itself; only whether it is there.
            Log.w(
                TAG,
                "YOUTUBE_API_KEY is missing from local.properties. YouTube features will show \"YouTube key missing\"."
            )
        }
    }

    private companion object {
        const val TAG = "FocusTube"
    }
}
