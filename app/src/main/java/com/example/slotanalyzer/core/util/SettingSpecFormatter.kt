package com.example.slotanalyzer.core.util

object SettingSpecFormatter {

    fun extractAvailableSettings(settingNumbers: List<Int>): List<Int> {
        return settingNumbers
            .distinct()
            .sorted()
    }

    fun formatStageLabel(settingNumbers: List<Int>): String {
        val settings = extractAvailableSettings(settingNumbers)
        if (settings.isEmpty()) return ""
        return "設定：" + settings.joinToString(" / ")
    }

    fun formatSummaryLabel(settingNumbers: List<Int>): String {
        val settings = extractAvailableSettings(settingNumbers)
        return when {
            settings == listOf(1, 2, 3, 4, 5, 6) -> "設定1〜6"
            settings.isEmpty() -> "設定1〜6"
            else -> "設定" + settings.joinToString("・")
        }
    }
}
