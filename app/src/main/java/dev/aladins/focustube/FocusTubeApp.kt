package dev.aladins.focustube

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import dev.aladins.focustube.di.PlainHttpClient
import javax.inject.Inject
import okhttp3.OkHttpClient

/**
 * The FocusTube application: the root of Hilt's dependency graph, and Coil's image loader
 * (thumbnails load through the plain OkHttp client, which never carries the YouTube key).
 */
@HiltAndroidApp
class FocusTubeApp :
    Application(),
    SingletonImageLoader.Factory {
    @Inject
    @PlainHttpClient
    lateinit var imageHttpClient: OkHttpClient

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

    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { imageHttpClient })) }
        .crossfade(true)
        .build()

    private companion object {
        const val TAG = "FocusTube"
    }
}
