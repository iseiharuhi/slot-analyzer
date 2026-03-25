package com.example.slotanalyzer.domain.model

data class CeilingCondition(
    val id: String,
    val title: String,
    val ceilingValue: Int,
    val type: CeilingType,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false,
    val isHighlighted: Boolean = false,
    val inputMode: InputMode = InputMode.TEXT,
    val stepValue: Int = 1,
    val showInput: Boolean = true,
    val benefitText: String? = null,
    val resetText: String? = null
)

enum class InputMode {
    TEXT,
    STEPPER,
    READ_ONLY
}
