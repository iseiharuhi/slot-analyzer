package com.example.slotanalyzer.feature.inference.presentation

import com.example.slotanalyzer.core.ui.model.CeilingStatusUiModel
import com.example.slotanalyzer.core.ui.model.ScoreBarItem

data class InferenceInputItemUiModel(
    val label: String,
    val valueText: String,
    val categoryLabel: String
)

data class InferenceReasonUiModel(
    val label: String,
    val valueText: String,
    val evaluationText: String,
    val levelLabel: String = "",
    val levelKey: String = "neutral"
)

data class SettingDistributionPointUiModel(
    val label: String,
    val value: Float
)

data class InferenceUiState(
    val sessionId: String = "",
    val machineName: String = "",
    val machineTypeText: String = "",
    val settingStageText: String = "",
    val probabilityModeText: String = "",
    val candidateSummaryText: String = "",
    val inputItems: List<InferenceInputItemUiModel> = emptyList(),
    val currentGameCount: Int = 0,
    val showCeilingSection: Boolean = true,
    val ceilingItems: List<CeilingStatusUiModel> = emptyList(),
    val reasonItems: List<InferenceReasonUiModel> = emptyList(),
    val reasonSummaryText: String = "",
    val summary: String = "",
    val confidenceText: String = "",
    val topSettingText: String = "",
    val settingBars: List<ScoreBarItem> = emptyList(),
    val bandBars: List<ScoreBarItem> = emptyList(),
    val settingDistributionPoints: List<SettingDistributionPointUiModel> = emptyList(),
    val isFinished: Boolean = false,
    val finishedStatusText: String = "",
    val isFinishing: Boolean = false,
    val finishCompleted: Boolean = false,
    val finishMessage: String? = null
)
