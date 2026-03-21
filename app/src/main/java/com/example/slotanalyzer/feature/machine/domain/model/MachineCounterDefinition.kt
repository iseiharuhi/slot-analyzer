package com.example.slotanalyzer.feature.machine.domain.model

import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.domain.model.CounterInputType

data class MachineCounterDefinition(
    val key: String,
    val displayName: String,
    val category: CounterCategory,
    val sortOrder: Int,
    val unit: String,
    val inputType: CounterInputType,
    val isEnabled: Boolean,
    val isDefaultVisible: Boolean,
    val supportsMinus: Boolean,
    val notes: String?
)
