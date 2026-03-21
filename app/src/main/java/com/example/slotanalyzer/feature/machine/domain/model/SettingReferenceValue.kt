package com.example.slotanalyzer.feature.machine.domain.model

data class SettingReferenceValue(
    val counterKey: String,
    val settingNo: Int,
    val denominatorValue: Double?,
    val numeratorValue: Double?,
    val weight: Double,
    val minSampleSize: Int?,
    val note: String?
)
