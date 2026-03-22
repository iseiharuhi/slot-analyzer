package com.example.slotanalyzer.feature.session.domain.model

import com.example.slotanalyzer.domain.model.ConfidenceLabel

data class PlaySession(
    val id: String,
    val machineId: String,
    val machineNameSnapshot: String,
    val title: String?,
    val startedAt: Long,
    val updatedAt: Long,
    val endedAt: Long?,
    val memo: String?,
    val isFinished: Boolean,
    val isCurrent: Boolean,
    val isArchivedInHistory: Boolean,
    val lastInferenceSummary: String?,
    val lastConfidenceLabel: ConfidenceLabel?,
    val counters: List<SessionCounterValue>,
    val ceilingStates: List<SessionCeilingState>
)
