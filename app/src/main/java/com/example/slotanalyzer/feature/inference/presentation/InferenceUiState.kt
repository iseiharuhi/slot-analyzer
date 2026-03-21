package com.example.slotanalyzer.feature.inference.presentation

import com.example.slotanalyzer.core.ui.model.ScoreBarItem

data class InferenceUiState(
    val machineName: String = "",
    val totalGames: Int = 0,
    val bigRateText: String = "--",
    val regRateText: String = "--",
    val czCount: Int = 0,
    val atCount: Int = 0,
    val summary: String = "",
    val confidenceText: String = "",
    val topSettingText: String = "",
    val settingBars: List<ScoreBarItem> = emptyList(),
    val bandBars: List<ScoreBarItem> = emptyList(),
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val saveMessage: String? = null
)