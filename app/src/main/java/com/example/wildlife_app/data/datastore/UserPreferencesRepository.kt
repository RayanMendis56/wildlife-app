
package com.example.wildlife_app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesRepository(
    private val context: Context
) {

    companion object {
        private val PREFER_COMMON_NAME =
            booleanPreferencesKey("prefer_common_name")

        private val SIGHTING_SORT_ORDER =
            stringPreferencesKey("sighting_sort_order")
    }

    val preferCommonName: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[PREFER_COMMON_NAME] ?: true
        }

    suspend fun setPreferCommonName(preferCommonName: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREFER_COMMON_NAME] = preferCommonName
        }
    }

    val sightingSortOrder: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[SIGHTING_SORT_ORDER] ?: "NEWEST_FIRST"
        }

    suspend fun setSightingSortOrder(sortOrder: String) {
        context.dataStore.edit { preferences ->
            preferences[SIGHTING_SORT_ORDER] = sortOrder
        }
    }
}
