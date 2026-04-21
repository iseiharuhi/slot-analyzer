package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.AppSettingDao
import com.example.slotanalyzer.data.database.entity.AppSettingEntity
import com.example.slotanalyzer.domain.model.MachineFilterKeys
import com.example.slotanalyzer.domain.model.MachineSortOrder
import com.example.slotanalyzer.domain.model.ThemeMode
import com.example.slotanalyzer.domain.model.UserPreferences
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingRepositoryImpl @Inject constructor(
    private val dao: AppSettingDao
) : AppSettingRepository {

    override fun observeUserPreferences(): Flow<UserPreferences> {
        return dao.observeSettings().map { settings ->
            val map = settings.associate { it.key to it.value }
            UserPreferences(
                themeMode = map[KEY_THEME_MODE] ?: ThemeMode.SYSTEM,
                showCeiling = (map[KEY_SHOW_CEILING] ?: "true").toBoolean(),
                showExternalLinks = (map[KEY_SHOW_EXTERNAL_LINKS] ?: "true").toBoolean(),
                machineSortOrder = map[KEY_MACHINE_SORT_ORDER] ?: MachineSortOrder.RELEASE_DATE,
                selectedMachineFilters = parseMachineFilters(
                    csvValue = map[KEY_SELECTED_MACHINE_FILTERS],
                    legacyValue = map[KEY_LEGACY_MACHINE_FILTER]
                ),
                hideUpcomingMachines = (map[KEY_HIDE_UPCOMING_MACHINES] ?: "false").toBoolean(),
                adsRemoved = (map[KEY_ADS_REMOVED] ?: "false").toBoolean(),
                defaultMachineId = map[KEY_DEFAULT_MACHINE_ID],
                historySortOrder = map[KEY_HISTORY_SORT_ORDER] ?: "date_desc"
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

    override suspend fun setSelectedMachineFilters(filters: Set<String>) {
        val normalized = filters
            .filter { it.isNotBlank() }
            .toSet()
            .ifEmpty { MachineFilterKeys.defaultSelected }
        upsert(KEY_SELECTED_MACHINE_FILTERS, normalized.joinToString(","))
    }

    override suspend fun setHideUpcomingMachines(hide: Boolean) {
        upsert(KEY_HIDE_UPCOMING_MACHINES, hide.toString())
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

    private fun parseMachineFilters(
        csvValue: String?,
        legacyValue: String?
    ): Set<String> {
        val csvFilters = csvValue
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()

        if (csvFilters.isNotEmpty()) {
            return csvFilters
        }

        return when (legacyValue) {
            null, "", "all" -> MachineFilterKeys.defaultSelected
            "at_smart" -> setOf(MachineFilterKeys.AT_SMART)
            "normal" -> setOf(MachineFilterKeys.NORMAL)
            "okinawa" -> setOf(MachineFilterKeys.OKINAWA)
            else -> MachineFilterKeys.defaultSelected
        }
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_SHOW_CEILING = "show_ceiling"
        const val KEY_SHOW_EXTERNAL_LINKS = "show_external_links"
        const val KEY_MACHINE_SORT_ORDER = "machine_sort_order"
        const val KEY_SELECTED_MACHINE_FILTERS = "selected_machine_filters"
        const val KEY_LEGACY_MACHINE_FILTER = "machine_filter"
        const val KEY_HIDE_UPCOMING_MACHINES = "hide_upcoming_machines"
        const val KEY_ADS_REMOVED = "ads_removed"
        const val KEY_DEFAULT_MACHINE_ID = "default_machine_id"
        const val KEY_HISTORY_SORT_ORDER = "history_sort_order"
    }
}
