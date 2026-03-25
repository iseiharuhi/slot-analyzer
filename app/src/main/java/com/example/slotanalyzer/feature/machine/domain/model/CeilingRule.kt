package com.example.slotanalyzer.feature.machine.domain.model

import com.example.slotanalyzer.domain.model.CeilingType

enum class CeilingInputMode {
    TEXT,
    STEPPER,
    READ_ONLY
}

data class CeilingRule(
    val ruleKey: String,
    val displayName: String,
    val ceilingType: CeilingType,
    val limitValue: Int,
    val unit: String,
    val resetOnHit: Boolean,
    val requiresResetFlag: Boolean,
    val description: String?,
    val isEnabled: Boolean,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false,
    val isHighlighted: Boolean = false,
    val inputMode: CeilingInputMode = CeilingInputMode.TEXT,
    val stepValue: Int = 1,
    val showInput: Boolean = true,
    val benefitText: String? = null,
    val resetText: String? = null
)
