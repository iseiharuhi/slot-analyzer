package com.example.slotanalyzer.domain.model.ceiling

data class CeilingStatus(
    val ruleKey: String,
    val displayName: String,
    val currentValue: Int,
    val limitValue: Int,
    val remainValue: Int,
    val unit: String,
    val description: String?
)
