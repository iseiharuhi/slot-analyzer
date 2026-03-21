package com.example.slotanalyzer.feature.history.domain.model

import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore

data class PlayHistory(
    val id: String,
    val sessionId: String,
    val machineId: String,
    val machineNameSnapshot: String,
    val playedDate: Long,
    val totalGames: Int,
    val bigCount: Int,
    val regCount: Int,
    val czCount: Int,
    val atCount: Int,
    val inferenceSummary: String,
    val confidenceLabel: ConfidenceLabel,
    val settingScores: List<SettingScore>,
    val memo: String?,
    val createdAt: Long
)