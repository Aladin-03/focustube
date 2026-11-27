package dev.aladins.focustube.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.model.HistoryEntry
import dev.aladins.focustube.domain.model.WatchLaterItem
import dev.aladins.focustube.domain.repository.HistoryRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.VideoRepository
import dev.aladins.focustube.domain.repository.WatchLaterRepository
import dev.aladins.focustube.domain.usecase.GroupByDayUseCase
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Library (16): the current persona's Watch later and History. Removing one item can be undone;
 * clearing History can't (the screen asks first). Opening the Library refreshes library video
 * details older than 30 days, at most once a day.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val personas: PersonaRepository,
    private val watchLater: WatchLaterRepository,
    private val history: HistoryRepository,
    videos: VideoRepository,
    groupByDay: GroupByDayUseCase,
    clock: Clock,
    zone: ZoneId
) : ViewModel() {
    private val tab = MutableStateFlow(LibraryTab.WatchLater)
    private val undo = MutableStateFlow<LibraryUndo?>(null)

    val uiState: StateFlow<LibraryUiState> =
        personas.observeCurrent().filterNotNull().flatMapLatest { persona ->
            combine(watchLater.observe(persona.id), history.observe(persona.id), tab, undo) { saved, watched, t, u ->
                LibraryUiState(
                    isLoading = false,
                    personaName = persona.name,
                    tab = t,
                    watchLater = saved,
                    historyDays = groupByDay(watched, zone) { it.lastWatchedAt },
                    today = clock.now().toJavaInstant().atZone(zone).toLocalDate(),
                    undo = u
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LibraryUiState())

    init {
        viewModelScope.launch { videos.refreshStaleSavedVideos() }
    }

    fun onTabChange(value: LibraryTab) {
        tab.value = value
    }

    fun onRemoveFromWatchLater(item: WatchLaterItem) = withPersona { id ->
        watchLater.remove(id, item.video.id)
        undo.value = LibraryUndo.WatchLater(item)
    }

    fun onRemoveFromHistory(entry: HistoryEntry) = withPersona { id ->
        history.remove(id, entry.video.id)
        undo.value = LibraryUndo.History(entry)
    }

    /** Puts the last removed item back. */
    fun onUndo() {
        val last = undo.value ?: return
        undo.value = null
        withPersona { id ->
            when (last) {
                is LibraryUndo.WatchLater -> watchLater.restore(id, last.item)
                is LibraryUndo.History -> history.restore(id, last.entry)
            }
        }
    }

    /** The Undo offer timed out or was dismissed. */
    fun onUndoDismissed() {
        undo.value = null
    }

    /** Clears this persona's History (the screen confirms first). */
    fun onClearHistory() = withPersona { id ->
        undo.value = null
        history.clear(id)
    }

    private fun withPersona(block: suspend (Long) -> Unit) {
        viewModelScope.launch {
            val persona = personas.observeCurrent().first() ?: return@launch
            block(persona.id)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
