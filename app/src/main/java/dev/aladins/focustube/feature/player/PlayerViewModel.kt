package dev.aladins.focustube.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.di.ApplicationScope
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.repository.HistoryRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.VideoRepository
import dev.aladins.focustube.domain.repository.WatchLaterRepository
import dev.aladins.focustube.domain.usecase.MarkWatchedUseCase
import dev.aladins.focustube.navigation.Route
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Player (14, 15). Details come from the saved video (0 units); only an unsaved video costs one
 * videos.list call.
 *
 * Watch progress goes to History through [MarkWatchedUseCase], without a write every second: only
 * after about 10 s of play, then every 15 s, when paused, the moment it counts as watched, when it
 * ends, and when the screen goes away. Writes run in the app scope so leaving never loses one.
 * A video not yet watched resumes where the persona stopped.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val videos: VideoRepository,
    private val personas: PersonaRepository,
    private val watchLater: WatchLaterRepository,
    private val history: HistoryRepository,
    private val markWatched: MarkWatchedUseCase,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {
    private val videoId: String = checkNotNull(savedStateHandle.get<String>(Route.Player.VIDEO_ID_KEY))
    private val playback = MutableStateFlow(PlayerUiState(videoId))

    private var personaId: Long? = null
    private var hadHistory = false
    private var playedSeconds = 0f
    private var lastReported = -1f
    private var lastSavedAt = -SAVE_EVERY_SECONDS
    private var savedAsWatched = false

    private val savedIds =
        personas.observeCurrent().filterNotNull().flatMapLatest { watchLater.observeIds(it.id) }

    val uiState: StateFlow<PlayerUiState> =
        combine(
            playback,
            videos.observe(videoId),
            personas.observeCurrent(),
            savedIds.onStart {
                emit(emptySet())
            }
        ) { state, video, persona, saved ->
            state.copy(
                video = video,
                personaName = persona?.name,
                isLoading = state.isLoading,
                isInWatchLater = videoId in saved
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PlayerUiState(videoId))

    init {
        viewModelScope.launch {
            val fetch = videos.fetchIfMissing(videoId)
            val persona = personas.observeCurrent().first()
            personaId = persona?.id
            val saved = persona?.let { history.getProgress(it.id, videoId) }
            hadHistory = saved != null
            savedAsWatched = saved?.isWatched == true
            val resumeAt =
                if (saved != null && !saved.isWatched &&
                    saved.positionSeconds < saved.durationSeconds - RESUME_TAIL_SECONDS
                ) {
                    saved.positionSeconds.toFloat()
                } else {
                    0f
                }
            playback.update {
                it.copy(isLoading = false, error = (fetch as? AppResult.Failure)?.error, positionSeconds = resumeAt)
            }
            lastReported = resumeAt
            lastSavedAt = resumeAt
        }
    }

    /** Called several times a second while playing. */
    fun onProgress(seconds: Float) {
        val delta = seconds - lastReported
        if (lastReported >= 0 && delta > 0 && delta <= MAX_TICK_SECONDS) playedSeconds += delta
        lastReported = seconds
        playback.update { it.copy(positionSeconds = seconds) }

        val video = currentVideo() ?: return
        if (!inHistory()) return
        val crossesIntoWatched =
            !savedAsWatched &&
                MarkWatchedUseCase.isWatched(seconds.toLong(), video.durationSeconds, video.isShort, ended = false)
        if (crossesIntoWatched || abs(seconds - lastSavedAt) >= SAVE_EVERY_SECONDS) save(video, seconds, ended = false)
    }

    fun onPaused() {
        // After the end, the full-length save stands; a later pause/leave mustn't overwrite it.
        if (playback.value.hasEnded) return
        val video = currentVideo() ?: return
        if (inHistory()) save(video, playback.value.positionSeconds, ended = false)
    }

    fun onEnded() {
        playback.update { it.copy(hasEnded = true) }
        val video = currentVideo() ?: return
        save(video, video.durationSeconds.toFloat(), ended = true)
    }

    /** The screen is going away (or the app to the background): save where we are. */
    fun onLeave() = onPaused()

    fun onReplay() {
        lastReported = 0f
        playback.update { it.copy(hasEnded = false, positionSeconds = 0f) }
    }

    fun onUnplayable() = playback.update { it.copy(isUnplayable = true) }

    fun onToggleWatchLater() {
        val id = personaId ?: return
        val saved = uiState.value.isInWatchLater
        viewModelScope.launch { if (saved) watchLater.remove(id, videoId) else watchLater.add(id, videoId) }
    }

    /** In History once it has played ~10 s, or if it was already there. */
    private fun inHistory(): Boolean = hadHistory || playedSeconds >= ENTER_HISTORY_SECONDS

    private fun currentVideo(): Video? = uiState.value.video ?: playback.value.video

    private fun save(video: Video, position: Float, ended: Boolean) {
        val id = personaId ?: return
        hadHistory = true
        lastSavedAt = position
        if (ended ||
            MarkWatchedUseCase.isWatched(position.toLong(), video.durationSeconds, video.isShort, ended)
        ) {
            savedAsWatched =
                true
        }
        appScope.launch {
            markWatched(id, videoId, position.toLong(), video.durationSeconds, video.isShort, ended)
        }
    }

    internal companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val ENTER_HISTORY_SECONDS = 10f
        const val SAVE_EVERY_SECONDS = 15f
        const val RESUME_TAIL_SECONDS = 10L

        /** Bigger jumps are seeks, not playback. */
        const val MAX_TICK_SECONDS = 2f
    }
}
