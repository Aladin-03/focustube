package dev.aladins.focustube.data.remote

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.remote.quota.QuotaInterceptor
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.data.remote.youtube.YouTubeKeyInterceptor
import dev.aladins.focustube.debug.redactedLoggingInterceptor
import dev.aladins.focustube.testing.FakeQuotaDao
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The real Retrofit + interceptor stack against a local server: key, quota and error mapping. */
class YouTubeNetworkTest {
    private val server = MockWebServer()
    private val quotaDao = FakeQuotaDao()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-27T20:00:00Z")
    }
    private val tracker = QuotaTracker(quotaDao, clock)
    private val json = Json { ignoreUnknownKeys = true }
    private val logLines = mutableListOf<String>()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.close()

    private fun dataSource(key: String = FAKE_KEY): YouTubeDataSource {
        val host = server.hostName
        val client =
            OkHttpClient.Builder()
                .addInterceptor(YouTubeKeyInterceptor(key, host))
                .addInterceptor(QuotaInterceptor(tracker, host))
                .addInterceptor(redactedLoggingInterceptor { logLines += it })
                .build()
        val api =
            Retrofit.Builder()
                .baseUrl(server.url("/youtube/v3/"))
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(YouTubeApi::class.java)
        return YouTubeDataSource(api, json)
    }

    private fun ok(body: String) = MockResponse.Builder().code(200).body(body).build()

    private fun error(code: Int, body: String) = MockResponse.Builder().code(code).body(body).build()

    private val today get() = quotaDao.rows.value["2026-11-27"] ?: 0

    @Test
    fun keyIsSentAsQueryParameter_andSearchCosts100_videosCost1() = runTest {
        server.enqueue(ok("""{"items":[{"id":{"videoId":"abc"}}],"nextPageToken":"t2"}"""))
        server.enqueue(ok("""{"items":[]}"""))
        val source = dataSource()

        val search = source.search("guitar", "US", null)
        assertTrue(search is AppResult.Success)
        assertEquals(FAKE_KEY, server.takeRequest().url.queryParameter("key"))
        assertEquals(100L, today)

        source.videos(listOf("abc"))
        assertEquals(101L, today)
    }

    @Test
    fun keyNeverAppearsInLogs() = runTest {
        server.enqueue(ok("""{"items":[]}"""))
        dataSource().search("guitar", "US", null)
        assertTrue("logged: $logLines", logLines.isNotEmpty())
        assertFalse(logLines.any { FAKE_KEY in it })
        // The key is replaced by "██" (URL-encoded in the logged URL).
        assertTrue("logged: $logLines", logLines.all { "key=%E2%96%88%E2%96%88" in it })
    }

    @Test
    fun requestOverTheLimitIsNeverSent() = runTest {
        quotaDao.rows.value = mapOf("2026-11-27" to 9_950L)
        val result = dataSource().search("guitar", "US", null)
        assertEquals(AppResult.Failure(AppError.QuotaExceeded), result)
        assertEquals(0, server.requestCount)
        assertEquals(9_950L, today)
    }

    @Test
    fun quotaExceeded403MapsToQuotaExceeded_andMarksTodayUsedUp() = runTest {
        server.enqueue(
            error(
                403,
                """{"error":{"code":403,"message":"quota","errors":[{"reason":"quotaExceeded","domain":"youtube.quota"}]}}"""
            )
        )
        val result = dataSource().search("guitar", "US", null)
        assertEquals(AppResult.Failure(AppError.QuotaExceeded), result)
        assertTrue(today >= QuotaTracker.DAILY_LIMIT)
        assertFalse(tracker.canSpendBlocking(1))
    }

    @Test
    fun invalidKeyMapsToKeyRejected() = runTest {
        server.enqueue(
            error(
                400,
                """{"error":{"code":400,"message":"API key not valid. Please pass a valid API key.","errors":[{"reason":"badRequest"}]}}"""
            )
        )
        assertEquals(AppResult.Failure(AppError.KeyRejected), dataSource().search("guitar", "US", null))
    }

    @Test
    fun apiNotEnabledMapsToKeyRejected() = runTest {
        server.enqueue(
            error(403, """{"error":{"code":403,"message":"disabled","errors":[{"reason":"accessNotConfigured"}]}}""")
        )
        assertEquals(AppResult.Failure(AppError.KeyRejected), dataSource().search("guitar", "US", null))
    }

    @Test
    fun serverErrorMapsToServer() = runTest {
        server.enqueue(error(503, "unavailable"))
        assertEquals(AppResult.Failure(AppError.Server(503)), dataSource().search("guitar", "US", null))
    }

    @Test
    fun missingKeyIsNeverSent() = runTest {
        assertEquals(AppResult.Failure(AppError.MissingYouTubeKey), dataSource(key = "").search("guitar", "US", null))
        assertEquals(0, server.requestCount)
        assertEquals(0L, today)
    }

    @Test
    fun connectionFailureMapsToNetwork() = runTest {
        val source = dataSource()
        server.close()
        assertEquals(AppResult.Failure(AppError.Network), source.search("guitar", "US", null))
    }

    private companion object {
        /** Not a real key; distinctive so a leak into logs is easy to spot. */
        const val FAKE_KEY = "fake-test-key-not-real-0123456789"
    }
}
