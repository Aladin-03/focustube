package dev.aladins.focustube.feature.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.RecentSearch
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.SavedSearch
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import dev.aladins.focustube.domain.model.ShortsFilter
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.QuotaRepository
import dev.aladins.focustube.domain.repository.SearchRepository
import dev.aladins.focustube.domain.repository.SettingsRepository
import dev.aladins.focustube.domain.usecase.LibraryMarks
import dev.aladins.focustube.domain.usecase.ObserveLibraryMarksUseCase
import dev.aladins.focustube.domain.usecase.ToggleWatchLaterUseCase
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Classic search (12) for the current persona. Saved results first: the same search again costs
 * 0 units; Refresh and "More results" call YouTube. Filtering never calls YouTube.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val personas: PersonaRepository,
    private val searches: SearchRepository,
    quota: QuotaRepository,
    settings: SettingsRepository,
    observeMarks: ObserveLibraryMarksUseCase,
    private val toggleWatchLater: ToggleWatchLaterUseCase
) : ViewModel() {
    /** The search field's text. */
    val query = TextFieldState()

    /** The persona's Watch later ids and watch progress (bookmarks and progress bars on rows). */
    private val library =
        personas.observeCurrent().filterNotNull().flatMapLatest { observeMarks(it.id) }.onStart { emit(LibraryMarks()) }

    private val submitted = MutableStateFlow<SearchKey?>(null)
    private val filter = MutableStateFlow(ShortsFilter.All)
    private val request = MutableStateFlow(RequestState())

    private val persona = personas.observeCurrent().filterNotNull()

    private val saved = submitted.flatMapLatest { key -> if (key == null) flowOf(null) else searches.observe(key) }

    private val recent =
        persona.flatMapLatest { p ->
            val regionsKey = p.regions.map { it.code }.sorted()
            searches.observeRecent(p.id, SearchMode.Classic).map { list ->
                list.filter { it.regions.map { r -> r.code }.sorted() == regionsKey }
            }
        }

    val uiState: StateFlow<SearchUiState> =
        combine(
            combine(
                persona,
                settings.observeDefaultRegions(),
                quota.observeToday().onStart<QuotaStatus?> {
                    emit(null)
                },
                ::Triple
            ),
            combine(submitted, saved, filter, ::Triple),
            combine(request, recent.onStart { emit(emptyList()) }, ::Pair),
            library
        ) { (p, defaults, q), (key, savedSearch, filterValue), (req, rec), marks ->
            buildState(
                p,
                defaults,
                q,
                key,
                savedSearch,
                filterValue,
                req,
                rec
            ).copy(marks = marks)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SearchUiState())

    init {
        // Clearing the field goes back to Recent.
        viewModelScope.launch {
            snapshotFlow { query.text.toString() }.collect { text ->
                if (text.isBlank()) {
                    submitted.value = null
                    request.value = RequestState()
                }
            }
        }
    }

    /** Searches for the field's text with the persona's regions (0 units if already saved). */
    fun onSearch() {
        viewModelScope.launch {
            val p = currentPersona() ?: return@launch
            search(p.id, p.regions, refresh = false)
        }
    }

    fun onRefresh() {
        val key = submitted.value ?: return
        run(isMore = false) { searches.search(key, refresh = true) }
    }

    fun onLoadMore() {
        val key = submitted.value ?: return
        run(isMore = true) { searches.loadMore(key) }
    }

    /** Saves or removes a result from the persona's Watch later. */
    fun onToggleWatchLater(videoId: String) {
        viewModelScope.launch {
            val p = currentPersona() ?: return@launch
            toggleWatchLater(p.id, videoId)
        }
    }

    fun onFilterChange(value: ShortsFilter) {
        filter.value = value
    }

    /** Opens a saved search (0 units). */
    fun onRecentClick(recent: RecentSearch) {
        query.setTextAndPlaceCursorAtEnd(recent.query)
        viewModelScope.launch {
            val p = currentPersona() ?: return@launch
            search(p.id, recent.regions, refresh = false)
        }
    }

    fun onRecentRemove(recent: RecentSearch) {
        viewModelScope.launch {
            val p = currentPersona() ?: return@launch
            searches.remove(SearchKey(p.id, recent.query, SearchMode.Classic, recent.regions))
        }
    }

    /** Saves the persona's regions; an open search runs again with them. */
    fun onRegionsApply(regions: List<Region>) {
        if (regions.isEmpty()) return
        viewModelScope.launch {
            val p = currentPersona() ?: return@launch
            personas.setRegions(p.id, regions)
            if (submitted.value != null) search(p.id, regions, refresh = false)
        }
    }

    private suspend fun currentPersona(): Persona? = personas.observeCurrent().first()

    private fun search(personaId: Long, regions: List<Region>, refresh: Boolean) {
        val text = query.text.toString().trim()
        if (text.isEmpty()) return
        val key = SearchKey(personaId, text, SearchMode.Classic, regions)
        submitted.value = key
        run(isMore = false) { searches.search(key, refresh) }
    }

    private fun run(isMore: Boolean, block: suspend () -> AppResult<Unit>) {
        request.value = RequestState(isLoading = !isMore, isLoadingMore = isMore)
        viewModelScope.launch {
            val result = block()
            request.update {
                RequestState(error = (result as? AppResult.Failure)?.error)
            }
        }
    }

    private fun buildState(
        persona: Persona,
        defaults: List<Region>,
        quota: QuotaStatus?,
        key: SearchKey?,
        saved: SavedSearch?,
        filterValue: ShortsFilter,
        request: RequestState,
        recent: List<RecentSearch>
    ) = SearchUiState(
        persona = persona,
        regions = persona.regions,
        defaultRegions = defaults,
        filter = filterValue,
        submittedQuery = key?.query,
        saved = saved,
        isLoading = request.isLoading,
        isLoadingMore = request.isLoadingMore,
        error = request.error,
        quota = quota,
        recent = recent
    )

    private data class RequestState(
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: AppError? = null
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
