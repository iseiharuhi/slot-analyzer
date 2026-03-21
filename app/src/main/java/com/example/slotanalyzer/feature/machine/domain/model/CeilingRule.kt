package com.example.slotanalyzer.feature.machine.domain.model

import com.example.slotanalyzer.domain.model.CeilingType

data class CeilingRule(
    val ruleKey: String,
    val displayName: String,
    val ceilingType: CeilingType,
    val limitValue: Int,
    val unit: String,
    val resetOnHit: Boolean,
    val requiresResetFlag: Boolean,
    val description: String?,
    val isEnabled: Boolean
)
