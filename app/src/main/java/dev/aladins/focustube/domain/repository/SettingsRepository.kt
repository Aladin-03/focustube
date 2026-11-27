package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.domain.model.Region
import kotlinx.coroutines.flow.Flow

/** App-wide settings that are not secret. */
interface SettingsRepository {
    /** Regions new personas start with (Settings > Default regions). */
    fun observeDefaultRegions(): Flow<List<Region>>

    suspend fun setDefaultRegions(regions: List<Region>)
}
