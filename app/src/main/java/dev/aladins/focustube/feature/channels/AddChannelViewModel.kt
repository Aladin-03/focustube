package dev.aladins.focustube.feature.channels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.repository.ChannelRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.QuotaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything Add channel (09) shows. */
data class AddChannelUiState(
    val results: List<Channel> = emptyList(),
    /** Channel id -> ids of personas following it. */
    val followers: Map<String, Set<Long>> = emptyMap(),
    val currentPersonaId: Long? = null,
    val personas: List<Persona> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val error: AppError? = null,
    val quota: QuotaStatus? = null,
    /** The channel whose "Add to…" sheet is open. */
    val sheetChannel: Channel? = null
) {
    /** "Added": the current persona already follows it. */
    fun isAdded(channel: Channel): Boolean =
        currentPersonaId != null && currentPersonaId in followers[channel.id].orEmpty()

    /** The sheet starts with the channel's followers plus the current persona ticked. */
    fun sheetTicks(channel: Channel): Set<Long> = followers[channel.id].orEmpty() + listOfNotNull(currentPersonaId)

    val isLimitReached: Boolean get() = quota?.isExhausted == true || error == AppError.QuotaExceeded
}

/**
 * Add channel (09): a link or @handle is looked up for 1 unit; words are searched (101 units,
 * remembered for 30 days). "Add" opens a persona checklist; saving sets who follows the channel
 * and, the first time, saves its first uploads (2 units).
 */
@HiltViewModel
class AddChannelViewModel @Inject constructor(
    private val channels: ChannelRepository,
    personas: PersonaRepository,
    quota: QuotaRepository
) : ViewModel() {
    /** The search field's text. */
    val query = TextFieldState()

    private val local = MutableStateFlow(AddChannelUiState())

    val uiState: StateFlow<AddChannelUiState> =
        combine(
            local,
            channels.observeAllFollowers(),
            personas.observeActive(),
            personas.observeCurrent(),
            quota.observeToday().onStart<QuotaStatus?> { emit(null) }
        ) { l, followers, active, current, q ->
            l.copy(followers = followers, personas = active, currentPersonaId = current?.id, quota = q)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddChannelUiState())

    fun onSearch() {
        val parsed = ChannelQuery.parse(query.text.toString()) ?: return
        local.update { it.copy(isSearching = true, error = null) }
        viewModelScope.launch {
            when (val result = channels.find(parsed)) {
                is AppResult.Success -> local.update {
                    it.copy(isSearching = false, hasSearched = true, results = result.value)
                }

                is AppResult.Failure -> local.update {
                    it.copy(isSearching = false, hasSearched = true, error = result.error)
                }
            }
        }
    }

    fun onAdd(channel: Channel) = local.update { it.copy(sheetChannel = channel) }

    fun onDismissSheet() = local.update { it.copy(sheetChannel = null) }

    /** Makes exactly [personaIds] follow the sheet's channel. */
    fun onSave(personaIds: Set<Long>) {
        val channel = local.value.sheetChannel ?: return
        local.update { it.copy(sheetChannel = null) }
        viewModelScope.launch {
            val result = channels.setFollowers(channel.id, personaIds)
            local.update { it.copy(error = (result as? AppResult.Failure)?.error) }
        }
    }
}
