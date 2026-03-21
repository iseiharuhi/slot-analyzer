package com.example.slotanalyzer.feature.inference.domain.model

import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.domain.model.SettingBand

data class ItemInferenceResult(
    val counterKey: String,
    val displayName: String,
    val observedRateText: String,
    val matchedBand: SettingBand,
    val confidenceLabel: ConfidenceLabel,
    val comment: String
)
