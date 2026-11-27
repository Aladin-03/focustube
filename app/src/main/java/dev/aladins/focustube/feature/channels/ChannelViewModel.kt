package dev.aladins.focustube.feature.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.repository.ChannelRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.QuotaRepository
import dev.aladins.focustube.domain.usecase.LibraryMarks
import dev.aladins.focustube.domain.usecase.ObserveLibraryMarksUseCase
import dev.aladins.focustube.domain.usecase.ToggleWatchLaterUseCase
import dev.aladins.focustube.navigation.Route
import javax.inject.Inject
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

/** How the channel page orders its uploads. ("Best for your goal" comes in v1.1.) */
enum class UploadSort { Newest, Popular }

/** Everything the channel page (10) shows. */
data class ChannelUiState(
    val channel: Channel? = null,
    val uploads: List<Video> = emptyList(),
    val sort: UploadSort = UploadSort.Newest,
    /** Active personas following it ("In: Guitar, General"). */
    val followers: List<Persona> = emptyList(),
    val followerIds: Set<Long> = emptySet(),
    val personas: List<Persona> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
    val quota: QuotaStatus? = null,
    val marks: LibraryMarks = LibraryMarks(),
    val isEditing: Boolean = false
) {
    val isLimitReached: Boolean get() = quota?.isExhausted == true || error == AppError.QuotaExceeded

    val canLoadMore: Boolean get() = channel?.hasMoreUploads == true && !isLoadingMore && !isLimitReached
}

/**
 * Channel page (10): header, who follows it, uploads (Newest, or Popular = loaded uploads by views,
 * 0 units), "More uploads" (2 units per 50). Opening a channel nobody follows saves its details and
 * first uploads (3 units).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChannelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val channels: ChannelRepository,
    private val personas: PersonaRepository,
    quota: QuotaRepository,
    observeMarks: ObserveLibraryMarksUseCase,
    private val toggleWatchLater: ToggleWatchLaterUseCase
) : ViewModel() {
    private val channelId: String = checkNotNull(savedStateHandle.get<String>(Route.Channel.CHANNEL_ID_KEY))
    private val local = MutableStateFlow(ChannelUiState())

    private val marks = personas.observeCurrent().filterNotNull().flatMapLatest {
        observeMarks(it.id)
    }.onStart { emit(LibraryMarks()) }

    val uiState: StateFlow<ChannelUiState> =
        combine(
            combine(channels.observe(channelId), channels.observeUploads(channelId), ::Pair),
            combine(channels.observeFollowers(channelId), personas.observeActive(), ::Pair),
            marks,
            quota.observeToday().onStart<QuotaStatus?> { emit(null) },
            local
        ) { (channel, uploads), (followerIds, active), m, q, l ->
            l.copy(
                channel = channel,
                uploads = if (l.sort ==
                    UploadSort.Popular
                ) {
                    uploads.sortedByDescending { it.viewCount ?: -1 }
                } else {
                    uploads
                },
                followers = active.filter { it.id in followerIds },
                followerIds = followerIds,
                personas = active,
                isLoading = l.isLoading && channel == null,
                marks = m,
                quota = q
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChannelUiState())

    init {
        viewModelScope.launch {
            val result = channels.ensureChannel(channelId)
            local.update { it.copy(isLoading = false, error = (result as? AppResult.Failure)?.error) }
        }
    }

    fun onSort(sort: UploadSort) = local.update { it.copy(sort = sort) }

    fun onLoadMore() {
        if (!uiState.value.canLoadMore) return
        local.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val result = channels.loadMoreUploads(channelId)
            local.update { it.copy(isLoadingMore = false, error = (result as? AppResult.Failure)?.error) }
        }
    }

    fun onEdit() = local.update { it.copy(isEditing = true) }

    fun onDismissEdit() = local.update { it.copy(isEditing = false) }

    /** Sets which personas follow this channel. */
    fun onSaveFollowers(personaIds: Set<Long>) {
        local.update { it.copy(isEditing = false) }
        viewModelScope.launch {
            val result = channels.setFollowers(channelId, personaIds)
            local.update { it.copy(error = (result as? AppResult.Failure)?.error) }
        }
    }

    fun onToggleWatchLater(videoId: String) {
        viewModelScope.launch {
            val persona = personas.observeCurrent().first() ?: return@launch
            toggleWatchLater(persona.id, videoId)
        }
    }
}
