package dev.aladins.focustube.data.remote.youtube

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/** Thrown before any request when the build has no YouTube key. */
class MissingYouTubeKeyException : IOException("YouTube key missing")

/**
 * Adds the YouTube key as the `key` query parameter, only for YouTube API requests. Never logs it;
 * the debug-only HTTP logger redacts it.
 */
class YouTubeKeyInterceptor(private val apiKey: String, private val host: String = YouTubeApi.HOST) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.host != host) return chain.proceed(request)
        if (apiKey.isBlank()) throw MissingYouTubeKeyException()
        val url = request.url.newBuilder().setQueryParameter(KEY_PARAM, apiKey).build()
        return chain.proceed(request.newBuilder().url(url).build())
    }

    companion object {
        const val KEY_PARAM = "key"
    }
}
