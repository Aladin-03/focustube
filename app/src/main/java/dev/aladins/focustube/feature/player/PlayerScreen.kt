package dev.aladins.focustube.feature.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import dev.aladins.focustube.R
import dev.aladins.focustube.core.designsystem.components.EmptyState
import dev.aladins.focustube.core.designsystem.components.FtTextButton
import dev.aladins.focustube.core.designsystem.components.PersonaAvatar
import dev.aladins.focustube.core.designsystem.components.PrimaryButton
import dev.aladins.focustube.core.designsystem.icons.FocusTubeIcons
import dev.aladins.focustube.core.designsystem.theme.FocusTubeTheme
import dev.aladins.focustube.core.ui.errorText
import dev.aladins.focustube.core.ui.videoMetaText
import dev.aladins.focustube.feature.player.components.EmbeddedYouTubePlayer
import dev.aladins.focustube.feature.player.components.WatchLaterAction
import kotlin.time.Clock

/**
 * Player (14) and Short (15): back, "Watching as [persona]", the embedded player (16:9, or 9:16
 * for a Short), title, meta, description and the end mark. When the video ends, FocusTube's own
 * end state covers the player. Landscape (or the full-screen button) shows only the player.
 * Actions: Watch later (thumbs and Channel arrive with Milestones 6 and 5).
 *
 * The player view stays in one place in the layout so rotating never restarts the video.
 */
@Composable
fun PlayerScreen(state: PlayerUiState, actions: PlayerActions, modifier: Modifier = Modifier) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var forcedFullscreen by rememberSaveable { mutableStateOf(false) }
    val fullscreen = (landscape || forcedFullscreen) && !state.isShort && state.video != null
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }

    FullscreenEffects(fullscreen = fullscreen, forceLandscape = forcedFullscreen && !state.isShort)

    val video = state.video
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(if (fullscreen) Color.Black else MaterialTheme.colorScheme.background)
                .then(if (fullscreen) Modifier else Modifier.windowInsetsPadding(WindowInsets.safeDrawing))
    ) {
        if (!fullscreen) PlayerHeader(personaName = state.personaName, onBack = actions.onBack)

        when {
            video != null ->
                BoxWithConstraints(
                    modifier = if (fullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val playerModifier =
                        when {
                            fullscreen -> Modifier.fillMaxSize()
                            state.isShort -> Modifier.height(minOf(maxWidth * 16f / 9f, 560.dp)).aspectRatio(9f / 16f)
                            else -> Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                        }
                    Box(playerModifier) {
                        EmbeddedYouTubePlayer(
                            videoId = state.videoId,
                            startSeconds = state.positionSeconds,
                            startPlaying = !state.hasEnded,
                            onProgress = actions.onProgress,
                            onPaused = actions.onPaused,
                            onEnded = actions.onEnded,
                            onUnplayable = actions.onUnplayable,
                            onPlayer = { player = it },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (state.hasEnded) {
                            EndState(
                                onReplay = {
                                    actions.onReplay()
                                    player?.seekTo(0f)
                                    player?.play()
                                }
                            )
                        }
                        if (state.isUnplayable) UnplayableState()
                        if (fullscreen) {
                            IconButton(
                                onClick = { forcedFullscreen = false },
                                modifier = Modifier.align(
                                    Alignment.TopEnd
                                ).windowInsetsPadding(WindowInsets.safeDrawing)
                            ) {
                                Icon(
                                    painterResource(FocusTubeIcons.FullscreenExit),
                                    contentDescription = stringResource(R.string.action_exit_fullscreen),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

            state.isLoading -> Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            state.error != null -> {
                val text = errorText(state.error)
                EmptyState(title = text.title, message = text.message, icon = text.icon, modifier = Modifier.weight(1f))
            }
        }

        if (!fullscreen && video != null) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = FocusTubeTheme.spacing.screenHorizontal)
            ) {
                Spacer(Modifier.height(FocusTubeTheme.spacing.lg))
                Text(
                    text = video.title,
                    style = FocusTubeTheme.typography.videoTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(FocusTubeTheme.spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = videoMetaText(video, remember(video.id) { Clock.System.now() }),
                        style = FocusTubeTheme.typography.meta,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (!state.isShort) {
                        IconButton(onClick = { forcedFullscreen = true }) {
                            Icon(
                                painterResource(FocusTubeIcons.Fullscreen),
                                contentDescription = stringResource(R.string.action_fullscreen),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                WatchLaterAction(isSaved = state.isInWatchLater, onToggle = actions.onToggleWatchLater)
                Spacer(Modifier.height(FocusTubeTheme.spacing.md))
                if (video.description.isNotBlank()) DescriptionCard(video.description)
                Text(
                    text = stringResource(R.string.player_end),
                    style = FocusTubeTheme.typography.mono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = FocusTubeTheme.spacing.xxl)
                )
            }
        }
    }
}

@Composable
private fun PlayerHeader(personaName: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = FocusTubeTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painterResource(FocusTubeIcons.Back),
                contentDescription = stringResource(R.string.action_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.weight(1f))
        if (personaName != null) {
            Surface(shape = FocusTubeTheme.shapes.pill, color = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                    Modifier.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PersonaAvatar(name = personaName, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.player_watching_as, personaName),
                        style = FocusTubeTheme.typography.label,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(Modifier.width(FocusTubeTheme.spacing.md))
    }
}

/** Covers YouTube's end screen so its suggestions are never seen. */
@Composable
private fun EndState(onReplay: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.player_end),
            style = FocusTubeTheme.typography.section,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = FocusTubeTheme.spacing.xl)
        )
        Spacer(Modifier.height(FocusTubeTheme.spacing.lg))
        PrimaryButton(text = stringResource(R.string.action_replay), onClick = onReplay)
    }
}

@Composable
private fun UnplayableState() {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.player_unplayable),
            style = FocusTubeTheme.typography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(FocusTubeTheme.spacing.xl)
        )
    }
}

@Composable
private fun DescriptionCard(description: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var cutOff by remember { mutableStateOf(false) }
    Surface(
        shape = FocusTubeTheme.shapes.card,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(FocusTubeTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.player_description).uppercase(),
                style = FocusTubeTheme.typography.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(FocusTubeTheme.spacing.sm))
            Text(
                text = description,
                style = FocusTubeTheme.typography.body,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                onTextLayout = { if (!expanded) cutOff = it.hasVisualOverflow }
            )
            // Only offer "Show more" when the description is actually cut off.
            if (cutOff || expanded) {
                FtTextButton(
                    text = stringResource(if (expanded) R.string.action_show_less else R.string.action_show_more),
                    onClick = { expanded = !expanded }
                )
            }
        }
    }
}

/** Full screen: hides system bars; the button version also locks landscape. Undone on leave. */
@Composable
private fun FullscreenEffects(fullscreen: Boolean, forceLandscape: Boolean) {
    val activity = LocalActivity.current ?: return
    val view = LocalView.current
    DisposableEffect(fullscreen, forceLandscape) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        if (fullscreen) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        activity.requestedOrientation =
            if (forceLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
