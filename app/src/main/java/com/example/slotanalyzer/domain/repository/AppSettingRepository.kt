package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface AppSettingRepository {
    fun observeUserPreferences(): Flow<UserPreferences>
}
