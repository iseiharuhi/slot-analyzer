package com.example.slotanalyzer.domain.model.ceiling

import com.example.slotanalyzer.domain.model.CeilingType

data class CeilingStatus(
    val ruleKey: String,
    val displayName: String,
    val currentValue: Int,
    val limitValue: Int,
    val remainValue: Int,
    val unit: String,
    val description: String?,
    val ceilingType: CeilingType,
    val isPrimary: Boolean,
    val isHighlighted: Boolean
)
