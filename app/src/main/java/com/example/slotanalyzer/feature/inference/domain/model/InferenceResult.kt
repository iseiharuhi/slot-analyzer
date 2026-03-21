package com.example.slotanalyzer.feature.inference.domain.model

import com.example.slotanalyzer.domain.model.ConfidenceLabel

data class InferenceResult(
    val summary: String,
    val confidenceLabel: ConfidenceLabel,
    val settingScores: List<SettingScore>
)