package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.AppSettingDao
import com.example.slotanalyzer.domain.model.UserPreferences
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingRepositoryImpl @Inject constructor(
    private val dao: AppSettingDao
) : AppSettingRepository {

    override fun observeUserPreferences(): Flow<UserPreferences> {
        return dao.observeSettings().map { settings ->
            val map = settings.associate { it.key to it.value }
            UserPreferences(
                themeMode = map["theme_mode"] ?: "system",
                adsRemoved = (map["ads_removed"] ?: "false").toBoolean(),
                defaultMachineId = map["default_machine_id"],
                historySortOrder = map["history_sort_order"] ?: "date_desc"
            )
        }
    }
}
