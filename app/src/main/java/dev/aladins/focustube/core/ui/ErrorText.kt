package dev.aladins.focustube.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.aladins.focustube.R
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons

/** What to show for an [AppError]: an icon, a title and what to do. */
data class ErrorText(val title: String, val message: String, @DrawableRes val icon: Int)

/** Plain-language text for every [AppError] (copy style: what happened and what to do). */
@Composable
fun errorText(error: AppError): ErrorText = when (error) {
    AppError.Network -> ErrorText(
        stringResource(R.string.error_offline_title),
        stringResource(R.string.error_offline_message),
        FocusTubeIcons.Offline
    )

    AppError.QuotaExceeded -> ErrorText(
        stringResource(R.string.limit_title),
        stringResource(R.string.limit_message),
        FocusTubeIcons.Clock
    )

    AppError.MissingYouTubeKey ->
        ErrorText(
            stringResource(R.string.error_key_missing_title),
            stringResource(R.string.error_key_missing_message),
            FocusTubeIcons.Info
        )

    AppError.KeyRejected ->
        ErrorText(
            stringResource(R.string.error_key_rejected_title),
            stringResource(R.string.error_key_rejected_message),
            FocusTubeIcons.Info
        )

    AppError.NotFound -> ErrorText(
        stringResource(R.string.error_not_found_title),
        stringResource(R.string.error_not_found_message),
        FocusTubeIcons.Info
    )

    is AppError.Server -> ErrorText(
        stringResource(R.string.error_server_title),
        stringResource(R.string.error_server_message),
        FocusTubeIcons.Info
    )

    AppError.Unknown -> ErrorText(
        stringResource(R.string.error_unknown_title),
        stringResource(R.string.error_unknown_message),
        FocusTubeIcons.Info
    )
}
