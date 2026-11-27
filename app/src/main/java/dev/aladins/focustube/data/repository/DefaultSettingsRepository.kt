package dev.aladins.focustube.data.repository

import dev.aladins.focustube.data.mapper.toRegions
import dev.aladins.focustube.data.mapper.toStoredRegions
import dev.aladins.focustube.data.settings.AppPreferences
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [SettingsRepository] on the plain preferences DataStore. Falls back to [Region.Defaults]. */
@Singleton
class DefaultSettingsRepository @Inject constructor(private val preferences: AppPreferences) : SettingsRepository {
    override fun observeDefaultRegions(): Flow<List<Region>> = preferences.defaultRegions.map { stored ->
        stored?.toRegions()?.takeIf { it.isNotEmpty() } ?: Region.Defaults
    }

    override suspend fun setDefaultRegions(regions: List<Region>) {
        require(regions.isNotEmpty()) { "At least one default region" }
        preferences.setDefaultRegions(regions.toStoredRegions())
    }
}
