package com.example.slotanalyzer.feature.session.domain.model

data class SessionCeilingState(
    val ruleKey: String,
    val currentValue: Int,
    val remainValue: Int,
    val updatedAt: Long
)
