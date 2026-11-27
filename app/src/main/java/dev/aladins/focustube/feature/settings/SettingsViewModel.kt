package dev.aladins.focustube.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.QuotaRepository
import dev.aladins.focustube.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings (17): default regions, the YouTube daily limit, archived personas. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    quota: QuotaRepository,
    personas: PersonaRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        combine(settings.observeDefaultRegions(), quota.observeToday(), personas.observeArchived()) {
                regions,
                q,
                archived
            ->
            SettingsUiState(defaultRegions = regions, quota = q, archivedCount = archived.size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState())

    fun onDefaultRegionsApply(regions: List<Region>) {
        if (regions.isEmpty()) return
        viewModelScope.launch { settings.setDefaultRegions(regions) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
