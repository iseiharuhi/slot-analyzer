package com.example.slotanalyzer.data.mapper

import com.example.slotanalyzer.domain.model.CeilingType
import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.domain.model.CounterInputType
import com.example.slotanalyzer.feature.machine.domain.model.CeilingInputMode

fun String.toCounterCategory(): CounterCategory = when (this) {
    "BASIC" -> CounterCategory.BASIC
    "BONUS" -> CounterCategory.BONUS
    "AT_CZ" -> CounterCategory.AT_CZ
    "SMALL_ROLE" -> CounterCategory.SMALL_ROLE
    "SPECIAL" -> CounterCategory.SPECIAL
    else -> CounterCategory.BASIC
}

fun String.toCounterInputType(): CounterInputType = when (this) {
    "COUNTER" -> CounterInputType.COUNTER
    "NUMBER" -> CounterInputType.NUMBER
    "DERIVED" -> CounterInputType.DERIVED
    else -> CounterInputType.COUNTER
}

fun String.toCeilingType(): CeilingType = when (this.uppercase()) {
    "GAME", "GAME_COUNT" -> CeilingType.GAME
    "COUNT", "THROUGH_COUNT" -> CeilingType.COUNT
    "CYCLE" -> CeilingType.CYCLE
    "POINT" -> CeilingType.POINT
    "COMPOSITE", "COMBINED" -> CeilingType.COMPOSITE
    else -> CeilingType.GAME
}

fun String?.toCeilingInputMode(defaultType: CeilingType): CeilingInputMode {
    return when (this?.uppercase()) {
        "TEXT" -> CeilingInputMode.TEXT
        "STEPPER" -> CeilingInputMode.STEPPER
        "READ_ONLY" -> CeilingInputMode.READ_ONLY
        else -> when (defaultType) {
            CeilingType.GAME, CeilingType.POINT -> CeilingInputMode.TEXT
            CeilingType.COUNT, CeilingType.CYCLE -> CeilingInputMode.STEPPER
            CeilingType.COMPOSITE -> CeilingInputMode.READ_ONLY
        }
    }
}

fun String.toConfidenceLabel(): ConfidenceLabel = when (this) {
    "HIGH" -> ConfidenceLabel.HIGH
    "MEDIUM" -> ConfidenceLabel.MEDIUM
    "LOW" -> ConfidenceLabel.LOW
    "INSUFFICIENT" -> ConfidenceLabel.INSUFFICIENT
    else -> ConfidenceLabel.TEMPORARY
}
