package dev.aladins.focustube.feature.settings

import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.model.Region

/** Everything Settings (17) shows in Milestone 3. The AI group arrives in Milestone 6. */
data class SettingsUiState(
    val defaultRegions: List<Region> = Region.Defaults,
    val quota: QuotaStatus? = null,
    val archivedCount: Int = 0
)
