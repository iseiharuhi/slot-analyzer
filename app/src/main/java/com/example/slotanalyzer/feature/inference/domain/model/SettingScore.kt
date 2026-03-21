package com.example.slotanalyzer.feature.inference.domain.model

data class SettingScore(
    val setting: Int,
    val rawValue: Double,
    val normalizedValue: Double
)