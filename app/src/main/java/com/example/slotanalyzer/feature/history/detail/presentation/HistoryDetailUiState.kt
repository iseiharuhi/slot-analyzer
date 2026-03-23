package com.example.slotanalyzer.feature.history.detail.presentation

import com.example.slotanalyzer.core.ui.model.ScoreBarItem

data class HistoryDetailInputItemUiModel(
    val label: String,
    val valueText: String,
    val categoryLabel: String
)

data class HistoryDetailReasonUiModel(
    val label: String,
    val valueText: String,
    val evaluationText: String
)

data class HistoryDetailUiState(
    val machineName: String = "",
    val machineTypeText: String = "",
    val playedAtText: String = "",
    val statusText: String = "",
    val inputItems: List<HistoryDetailInputItemUiModel> = emptyList(),
    val reasonItems: List<HistoryDetailReasonUiModel> = emptyList(),
    val summary: String = "",
    val confidenceText: String = "",
    val topSettingText: String = "",
    val settingBars: List<ScoreBarItem> = emptyList(),
    val bandBars: List<ScoreBarItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
