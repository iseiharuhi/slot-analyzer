package com.example.slotanalyzer.feature.history.detail.presentation

import com.example.slotanalyzer.core.ui.model.ScoreBarItem

data class HistoryDetailUiState(
    val machineName: String = "",
    val playedAtText: String = "",
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
    val isLoading: Boolean = true
)