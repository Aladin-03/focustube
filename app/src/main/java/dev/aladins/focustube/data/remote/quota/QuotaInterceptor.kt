package dev.aladins.focustube.data.remote.quota

import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/** Thrown instead of sending a request that would pass today's limit. */
class QuotaExceededException : IOException("YouTube daily limit reached")

/** Unit cost of each YouTube endpoint (YouTube's quota table). */
object YouTubeCosts {
    const val SEARCH = 100L
    const val VIDEOS = 1L

    /** Cost by the last path segment; unknown endpoints count as 1. */
    fun forPath(lastSegment: String): Long = when (lastSegment) {
        "search" -> SEARCH
        else -> VIDEOS
    }
}

/**
 * Every YouTube request goes through here: refused if it would pass today's limit, otherwise
 * charged once a response arrives (YouTube charges failed requests too). A 403 quotaExceeded
 * marks today as used up.
 */
class QuotaInterceptor(private val tracker: QuotaTracker, private val host: String = YouTubeApi.HOST) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.host != host) return chain.proceed(request)

        val cost = YouTubeCosts.forPath(request.url.pathSegments.lastOrNull().orEmpty())
        if (!tracker.canSpendBlocking(cost)) throw QuotaExceededException()

        val response = chain.proceed(request)
        tracker.chargeBlocking(cost)
        if (response.code == 403 && isQuotaError(response)) tracker.markExhaustedBlocking()
        return response
    }

    private fun isQuotaError(response: Response): Boolean {
        val body = response.peekBody(PEEK_BYTES).string()
        return QUOTA_REASONS.any { "\"$it\"" in body }
    }

    private companion object {
        const val PEEK_BYTES = 16_384L
        val QUOTA_REASONS = listOf("quotaExceeded", "dailyLimitExceeded")
    }
}
