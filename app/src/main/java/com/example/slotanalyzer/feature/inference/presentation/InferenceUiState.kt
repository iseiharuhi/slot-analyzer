package com.example.slotanalyzer.feature.inference.presentation

import com.example.slotanalyzer.core.ui.model.ScoreBarItem

data class InferenceInputItemUiModel(
    val label: String,
    val valueText: String,
    val categoryLabel: String
)

data class InferenceReasonUiModel(
    val label: String,
    val valueText: String,
    val evaluationText: String
)

data class InferenceUiState(
    val sessionId: String = "",
    val machineName: String = "",
    val machineTypeText: String = "",
    val inputItems: List<InferenceInputItemUiModel> = emptyList(),
    val reasonItems: List<InferenceReasonUiModel> = emptyList(),
    val summary: String = "",
    val confidenceText: String = "",
    val topSettingText: String = "",
    val settingBars: List<ScoreBarItem> = emptyList(),
    val bandBars: List<ScoreBarItem> = emptyList(),
    val isFinished: Boolean = false,
    val finishedStatusText: String = "",
    val isFinishing: Boolean = false,
    val finishCompleted: Boolean = false,
    val finishMessage: String? = null
)
