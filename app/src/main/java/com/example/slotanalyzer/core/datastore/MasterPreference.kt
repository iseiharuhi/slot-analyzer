package com.example.slotanalyzer.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MasterPreference @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val VERSION = intPreferencesKey("master_version")
        val LAST_CHECKED = longPreferencesKey("last_checked_at")
    }

    suspend fun getVersion(): Int {
        return dataStore.data.first()[VERSION] ?: 0
    }

    suspend fun setVersion(version: Int) {
        dataStore.edit { it[VERSION] = version }
    }

    suspend fun getLastChecked(): Long {
        return dataStore.data.first()[LAST_CHECKED] ?: 0L
    }

    suspend fun setLastChecked(time: Long) {
        dataStore.edit { it[LAST_CHECKED] = time }
    }
}
