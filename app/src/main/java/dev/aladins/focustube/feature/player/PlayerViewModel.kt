package dev.aladins.focustube.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.VideoRepository
import dev.aladins.focustube.navigation.Route
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Player (14, 15). Details come from the saved video (0 units); only an unsaved video costs one
 * videos.list call. Nothing about watching is stored yet: progress stays in memory.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    videos: VideoRepository,
    personas: PersonaRepository
) : ViewModel() {
    private val videoId: String = checkNotNull(savedStateHandle.get<String>(Route.Player.VIDEO_ID_KEY))
    private val playback = MutableStateFlow(PlayerUiState(videoId))

    val uiState: StateFlow<PlayerUiState> =
        combine(playback, videos.observe(videoId), personas.observeCurrent()) { state, video, persona ->
            state.copy(video = video, personaName = persona?.name, isLoading = state.isLoading && video == null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PlayerUiState(videoId))

    init {
        viewModelScope.launch {
            val result = videos.fetchIfMissing(videoId)
            playback.update { it.copy(isLoading = false, error = (result as? AppResult.Failure)?.error) }
        }
    }

    fun onProgress(seconds: Float) = playback.update { it.copy(positionSeconds = seconds) }

    fun onEnded() = playback.update { it.copy(hasEnded = true) }

    fun onReplay() = playback.update { it.copy(hasEnded = false, positionSeconds = 0f) }

    fun onUnplayable() = playback.update { it.copy(isUnplayable = true) }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
