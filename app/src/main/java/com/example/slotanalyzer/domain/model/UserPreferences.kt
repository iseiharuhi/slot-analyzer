package com.example.slotanalyzer.domain.model

data class UserPreferences(
    val themeMode: String = "system",
    val adsRemoved: Boolean = false,
    val defaultMachineId: String? = null,
    val historySortOrder: String = "date_desc"
)
