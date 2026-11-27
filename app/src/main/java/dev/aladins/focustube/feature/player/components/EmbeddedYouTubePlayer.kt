package dev.aladins.focustube.feature.player.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * YouTube's official embedded player (IFrame API in a WebView, via android-youtube-player).
 * Controls on; related videos limited to the same channel (rel=0); annotations off; nothing is
 * queued after the video. YouTube's fullscreen button is off: rotating, or FocusTube's own button,
 * goes full screen. The tapped video starts playing at once.
 *
 * Size it with [modifier]; it fills it. [onPlayer] hands out the player once it's ready (Replay).
 * When the view is recreated (e.g. a theme change), playback resumes at [startSeconds]; if
 * [startPlaying] is false (the video had ended) it is only loaded, so nothing plays hidden.
 */
@Composable
fun EmbeddedYouTubePlayer(
    videoId: String,
    startSeconds: Float,
    startPlaying: Boolean,
    onProgress: (Float) -> Unit,
    onPaused: () -> Unit,
    onEnded: () -> Unit,
    onUnplayable: () -> Unit,
    onPlayer: (YouTubePlayer) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val progress by rememberUpdatedState(onProgress)
    val paused by rememberUpdatedState(onPaused)
    val ended by rememberUpdatedState(onEnded)
    val unplayable by rememberUpdatedState(onUnplayable)
    val ready by rememberUpdatedState(onPlayer)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                // Fill the Compose box (not the library's default 16:9 wrap), so Shorts can be 9:16.
                layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                lifecycle.addObserver(this)
                val options =
                    IFramePlayerOptions.Builder(context)
                        .controls(1)
                        .rel(0)
                        .ivLoadPolicy(3)
                        .fullscreen(0)
                        .build()
                initialize(
                    object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            ready(youTubePlayer)
                            if (startPlaying) {
                                youTubePlayer.loadVideo(videoId, startSeconds)
                            } else {
                                youTubePlayer.cueVideo(videoId, startSeconds)
                            }
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            when (state) {
                                PlayerConstants.PlayerState.ENDED -> ended()
                                PlayerConstants.PlayerState.PAUSED -> paused()
                                else -> Unit
                            }
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) = progress(second)

                        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) =
                            unplayable()
                    },
                    true,
                    options
                )
            }
        },
        onRelease = { view ->
            lifecycle.removeObserver(view)
            view.release()
        }
    )
}
