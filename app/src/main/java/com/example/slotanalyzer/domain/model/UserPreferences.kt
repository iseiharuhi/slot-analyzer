package com.example.slotanalyzer.domain.model

data class UserPreferences(
    val themeMode: String = ThemeMode.SYSTEM,
    val adsRemoved: Boolean = false,
    val defaultMachineId: String? = null,
    val historySortOrder: String = HistorySortOrder.DATE_DESC,
    val showCeiling: Boolean = true,
    val showExternalLinks: Boolean = true,
    val machineSortOrder: String = MachineSortOrder.RELEASE_DATE,
    val machineFilter: String = MachineFilter.ALL
)

object ThemeMode {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"
}

object HistorySortOrder {
    const val DATE_DESC = "date_desc"
}

object MachineSortOrder {
    const val RELEASE_DATE = "release_date"
    const val NAME = "name"
}

object MachineFilter {
    const val ALL = "all"
    const val AT_SMART = "at_smart"
    const val NORMAL = "normal"
    const val OKINAWA = "okinawa"
}
