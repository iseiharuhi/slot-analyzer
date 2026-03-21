package com.example.slotanalyzer.feature.inference.presentation

import com.example.slotanalyzer.core.ui.model.ScoreBarItem

data class InferenceInputItemUiModel(
    val label: String,
    val valueText: String
)

data class InferenceUiState(
    val machineName: String = "",
    val inputItems: List<InferenceInputItemUiModel> = emptyList(),
    val summary: String = "",
    val confidenceText: String = "",
    val topSettingText: String = "",
    val settingBars: List<ScoreBarItem> = emptyList(),
    val bandBars: List<ScoreBarItem> = emptyList(),
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val saveMessage: String? = null
)