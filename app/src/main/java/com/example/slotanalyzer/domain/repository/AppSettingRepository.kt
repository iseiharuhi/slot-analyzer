package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface AppSettingRepository {
    fun observeUserPreferences(): Flow<UserPreferences>
    suspend fun setThemeMode(themeMode: String)
    suspend fun setShowCeiling(show: Boolean)
    suspend fun setShowExternalLinks(show: Boolean)
    suspend fun setMachineSortOrder(sortOrder: String)
    suspend fun setSelectedMachineFilters(filters: Set<String>)
}
