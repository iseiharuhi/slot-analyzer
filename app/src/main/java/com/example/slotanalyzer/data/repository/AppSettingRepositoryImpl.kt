package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.AppSettingDao
import com.example.slotanalyzer.data.database.entity.AppSettingEntity
import com.example.slotanalyzer.domain.model.HistorySortOrder
import com.example.slotanalyzer.domain.model.MachineFilter
import com.example.slotanalyzer.domain.model.MachineSortOrder
import com.example.slotanalyzer.domain.model.ThemeMode
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
                themeMode = map[KEY_THEME_MODE] ?: ThemeMode.SYSTEM,
                adsRemoved = (map[KEY_ADS_REMOVED] ?: "false").toBoolean(),
                defaultMachineId = map[KEY_DEFAULT_MACHINE_ID],
                historySortOrder = map[KEY_HISTORY_SORT_ORDER] ?: HistorySortOrder.DATE_DESC,
                showCeiling = (map[KEY_SHOW_CEILING] ?: "true").toBoolean(),
                showExternalLinks = (map[KEY_SHOW_EXTERNAL_LINKS] ?: "true").toBoolean(),
                machineSortOrder = map[KEY_MACHINE_SORT_ORDER] ?: MachineSortOrder.RELEASE_DATE,
                machineFilter = map[KEY_MACHINE_FILTER] ?: MachineFilter.ALL
            )
        }
    }

    override suspend fun setThemeMode(themeMode: String) {
        upsert(KEY_THEME_MODE, themeMode)
    }

    override suspend fun setShowCeiling(show: Boolean) {
        upsert(KEY_SHOW_CEILING, show.toString())
    }

    override suspend fun setShowExternalLinks(show: Boolean) {
        upsert(KEY_SHOW_EXTERNAL_LINKS, show.toString())
    }

    override suspend fun setMachineSortOrder(sortOrder: String) {
        upsert(KEY_MACHINE_SORT_ORDER, sortOrder)
    }

    override suspend fun setMachineFilter(filter: String) {
        upsert(KEY_MACHINE_FILTER, filter)
    }

    private suspend fun upsert(key: String, value: String) {
        dao.upsert(
            AppSettingEntity(
                key = key,
                value = value,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_ADS_REMOVED = "ads_removed"
        const val KEY_DEFAULT_MACHINE_ID = "default_machine_id"
        const val KEY_HISTORY_SORT_ORDER = "history_sort_order"
        const val KEY_SHOW_CEILING = "show_ceiling"
        const val KEY_SHOW_EXTERNAL_LINKS = "show_external_links"
        const val KEY_MACHINE_SORT_ORDER = "machine_sort_order"
        const val KEY_MACHINE_FILTER = "machine_filter"
    }
}
