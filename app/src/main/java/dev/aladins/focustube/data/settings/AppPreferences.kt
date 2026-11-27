package dev.aladins.focustube.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Small, non-secret app preferences in a plain DataStore. (Secrets such as the OpenAI key go in
 * the encrypted SettingsStore instead.)
 */
@Singleton
class AppPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {
    /** Id of the persona the user last chose, or null if none was chosen yet. */
    val currentPersonaId: Flow<Long?> = dataStore.data.map { it[CURRENT_PERSONA_ID] }

    suspend fun setCurrentPersonaId(id: Long) {
        dataStore.edit { it[CURRENT_PERSONA_ID] = id }
    }

    /** Default regions as stored ("US,GB,DE"), or null if never changed. */
    val defaultRegions: Flow<String?> = dataStore.data.map { it[DEFAULT_REGIONS] }

    suspend fun setDefaultRegions(stored: String) {
        dataStore.edit { it[DEFAULT_REGIONS] = stored }
    }

    private companion object {
        val CURRENT_PERSONA_ID = longPreferencesKey("current_persona_id")
        val DEFAULT_REGIONS = stringPreferencesKey("default_regions")
    }
}
