package com.example.slotanalyzer.feature.session.domain.model

data class SessionCounterValue(
    val counterKey: String,
    val intValue: Int? = null,
    val doubleValue: Double? = null,
    val updatedAt: Long
)
