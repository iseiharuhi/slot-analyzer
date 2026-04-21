package com.example.slotanalyzer.domain.model

data class UserPreferences(
    val themeMode: String = ThemeMode.SYSTEM,
    val keepScreenOn: Boolean = false,
    val showCeiling: Boolean = true,
    val showExternalLinks: Boolean = true,
    val machineSortOrder: String = MachineSortOrder.RELEASE_DATE,
    val selectedMachineFilters: Set<String> = MachineFilterKeys.defaultSelected,
    val hideUpcomingMachines: Boolean = false,
    val adsRemoved: Boolean = false,
    val defaultMachineId: String? = null,
    val historySortOrder: String = "date_desc"
)
