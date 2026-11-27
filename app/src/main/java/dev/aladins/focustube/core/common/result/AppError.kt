package dev.aladins.focustube.core.common.result

/** Every failure the UI needs to tell apart. Screens map each to plain text (core/ui/AppErrorText). */
sealed interface AppError {
    /** Offline, timeout or the connection dropped. */
    data object Network : AppError

    /** YouTube's shared daily limit is used up (or would be by this request). */
    data object QuotaExceeded : AppError

    /** The build has no YouTube key (local.properties). */
    data object MissingYouTubeKey : AppError

    /** YouTube refused the key (invalid, API not enabled, or a restriction mismatch). */
    data object KeyRejected : AppError

    /** The video or item no longer exists. */
    data object NotFound : AppError

    /** Any other HTTP error from YouTube. */
    data class Server(val code: Int) : AppError

    data object Unknown : AppError
}

/** Result of a call that can fail with an [AppError]. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}
