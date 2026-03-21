package com.example.slotanalyzer.feature.session.domain.model

data class UpdateCounterCommand(
    val sessionId: String,
    val counterKey: String,
    val intValue: Int? = null,
    val doubleValue: Double? = null,
    val updatedAt: Long
)
