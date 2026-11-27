package dev.aladins.focustube.data.remote.youtube

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.remote.quota.QuotaExceededException
import dev.aladins.focustube.data.remote.youtube.dto.ChannelDto
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItemListResponse
import dev.aladins.focustube.data.remote.youtube.dto.SearchListResponse
import dev.aladins.focustube.data.remote.youtube.dto.VideoDto
import dev.aladins.focustube.data.remote.youtube.dto.YouTubeErrorResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * The only place that calls [YouTubeApi]. Turns every failure into an [AppError]; never includes
 * URLs (which carry the key) in errors or logs.
 */
@Singleton
class YouTubeDataSource @Inject constructor(private val api: YouTubeApi, private val json: Json) {
    /** One page of video ids (100 units). */
    suspend fun search(query: String, regionCode: String, pageToken: String?): AppResult<SearchListResponse> = call {
        api.search(query = query, regionCode = regionCode, pageToken = pageToken)
    }

    /** Details for up to 50 ids per call (1 unit per call). Missing ids are simply absent. */
    suspend fun videos(ids: List<String>): AppResult<List<VideoDto>> {
        val all = mutableListOf<VideoDto>()
        for (chunk in ids.distinct().chunked(YouTubeApi.MAX_RESULTS)) {
            when (val page = call { api.videos(chunk.joinToString(",")) }) {
                is AppResult.Success -> all += page.value.items
                is AppResult.Failure -> return page
            }
        }
        return AppResult.Success(all)
    }

    /** Ids of channels matching [query] (100 units). */
    suspend fun searchChannels(query: String): AppResult<List<String>> = call {
        api.searchChannels(query).items.mapNotNull { it.id.channelId }.distinct()
    }

    /** Channel details by ids, 50 per call (1 unit per call). */
    suspend fun channels(ids: List<String>): AppResult<List<ChannelDto>> {
        val all = mutableListOf<ChannelDto>()
        for (chunk in ids.distinct().chunked(YouTubeApi.MAX_RESULTS)) {
            when (val page = call { api.channels(ids = chunk.joinToString(",")) }) {
                is AppResult.Success -> all += page.value.items
                is AppResult.Failure -> return page
            }
        }
        return AppResult.Success(all)
    }

    /** A channel by @handle (without the @) or old username (1 unit); empty if none. */
    suspend fun channelBy(handle: String? = null, username: String? = null): AppResult<List<ChannelDto>> = call {
        api.channels(handle = handle, username = username).items
    }

    /** One page of an uploads playlist (1 unit). */
    suspend fun uploads(playlistId: String, pageToken: String?): AppResult<PlaylistItemListResponse> = call {
        api.playlistItems(playlistId = playlistId, pageToken = pageToken)
    }

    private suspend fun <T> call(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: QuotaExceededException) {
        AppResult.Failure(AppError.QuotaExceeded)
    } catch (e: MissingYouTubeKeyException) {
        AppResult.Failure(AppError.MissingYouTubeKey)
    } catch (e: HttpException) {
        AppResult.Failure(mapHttpError(e.code(), e.response()?.errorBody()?.string().orEmpty()))
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (e: kotlinx.serialization.SerializationException) {
        AppResult.Failure(AppError.Unknown)
    }

    /** Maps an HTTP status and YouTube error body to an [AppError]. */
    internal fun mapHttpError(code: Int, body: String): AppError {
        val parsed = runCatching { json.decodeFromString<YouTubeErrorResponse>(body).error }.getOrNull()
        val reasons = parsed?.errors.orEmpty().map { it.reason } + parsed?.details.orEmpty().map { it.reason }
        return when {
            reasons.any { it == "quotaExceeded" || it == "dailyLimitExceeded" } -> AppError.QuotaExceeded
            reasons.any { it in KEY_REASONS } -> AppError.KeyRejected
            code == 400 && parsed?.message.orEmpty().contains("API key", ignoreCase = true) -> AppError.KeyRejected
            code == 404 -> AppError.NotFound
            else -> AppError.Server(code)
        }
    }

    private companion object {
        val KEY_REASONS =
            setOf(
                "keyInvalid",
                "API_KEY_INVALID",
                "accessNotConfigured",
                "SERVICE_DISABLED",
                "API_KEY_SERVICE_BLOCKED",
                "API_KEY_HTTP_REFERRER_BLOCKED",
                "API_KEY_ANDROID_APP_BLOCKED",
                "ipRefererBlocked",
                "forbidden"
            )
    }
}
