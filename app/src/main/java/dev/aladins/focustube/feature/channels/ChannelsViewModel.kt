package dev.aladins.focustube.feature.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.repository.ChannelRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.QuotaRepository
import dev.aladins.focustube.domain.usecase.CheckUploadsUseCase
import dev.aladins.focustube.domain.usecase.GroupByDayUseCase
import dev.aladins.focustube.domain.usecase.LibraryMarks
import dev.aladins.focustube.domain.usecase.ObserveLibraryMarksUseCase
import dev.aladins.focustube.domain.usecase.ToggleWatchLaterUseCase
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant
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
 * My channels (08): the persona's New uploads (last 30 days, by day) and its channels. "Check now"
 * checks this persona's channels (1 unit each, plus 1 per 50 new videos). Removing a channel from
 * the persona can be undone.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val personas: PersonaRepository,
    private val channels: ChannelRepository,
    quota: QuotaRepository,
    observeMarks: ObserveLibraryMarksUseCase,
    private val toggleWatchLater: ToggleWatchLaterUseCase,
    private val checkUploads: CheckUploadsUseCase,
    groupByDay: GroupByDayUseCase,
    private val clock: Clock,
    zone: ZoneId
) : ViewModel() {
    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<ChannelsUiState> =
        personas.observeCurrent().filterNotNull().flatMapLatest { persona ->
            combine(
                combine(
                    channels.observeFeed(persona.id, clock.now() - FEED_WINDOW),
                    channels.observeFollowed(persona.id),
                    channels.observeLastChecked(persona.id),
                    ::Triple
                ),
                observeMarks(persona.id).onStart { emit(LibraryMarks()) },
                quota.observeToday().onStart<QuotaStatus?> { emit(null) },
                local
            ) { (feed, followed, lastChecked), marks, q, l ->
                ChannelsUiState(
                    isLoading = false,
                    personaName = persona.name,
                    tab = l.tab,
                    feed = groupByDay(feed, zone) { it.publishedAt },
                    channels = followed,
                    lastCheckedAt = lastChecked,
                    isChecking = l.isChecking,
                    error = l.error,
                    quota = q,
                    marks = marks,
                    undo = l.undo,
                    today = clock.now().toJavaInstant().atZone(zone).toLocalDate()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ChannelsUiState())

    fun onTabChange(tab: ChannelsTab) = local.update { it.copy(tab = tab) }

    /** Checks this persona's channels now. */
    fun onCheckNow() {
        if (local.value.isChecking) return
        local.update { it.copy(isChecking = true, error = null) }
        withPersona { id ->
            val result = checkUploads(personaId = id, force = true)
            local.update { it.copy(isChecking = false, error = (result as? AppResult.Failure)?.error) }
        }
    }

    fun onToggleWatchLater(videoId: String) = withPersona { id -> toggleWatchLater(id, videoId) }

    /** Removes the channel from this persona only (other personas keep it). */
    fun onUnfollow(channel: Channel) = withPersona { id ->
        val since = channels.followedSince(id, channel.id) ?: return@withPersona
        channels.unfollow(id, channel.id)
        local.update { it.copy(undo = UnfollowUndo(channel, since)) }
    }

    fun onUndo() {
        val last = local.value.undo ?: return
        local.update { it.copy(undo = null) }
        withPersona { id -> channels.restoreFollow(id, last.channel.id, last.addedAt) }
    }

    fun onUndoDismissed() = local.update { it.copy(undo = null) }

    private fun withPersona(block: suspend (Long) -> Unit) {
        viewModelScope.launch {
            val persona = personas.observeCurrent().first() ?: return@launch
            block(persona.id)
        }
    }

    private data class Local(
        val tab: ChannelsTab = ChannelsTab.NewUploads,
        val isChecking: Boolean = false,
        val error: AppError? = null,
        val undo: UnfollowUndo? = null
    )

    internal companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L

        /** The New uploads feed shows this far back (matches the 30-day cache policy). */
        val FEED_WINDOW = 30.days
    }
}
