package com.example.slotanalyzer.data.mapper

import com.example.slotanalyzer.domain.model.*

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

fun String.toCeilingType(): CeilingType = when (this) {
    "GAME_COUNT" -> CeilingType.GAME_COUNT
    "THROUGH_COUNT" -> CeilingType.THROUGH_COUNT
    "COMBINED" -> CeilingType.COMBINED
    else -> CeilingType.GAME_COUNT
}

fun String.toConfidenceLabel(): ConfidenceLabel = when (this) {
    "HIGH" -> ConfidenceLabel.HIGH
    "MEDIUM" -> ConfidenceLabel.MEDIUM
    "LOW" -> ConfidenceLabel.LOW
    "INSUFFICIENT" -> ConfidenceLabel.INSUFFICIENT
    else -> ConfidenceLabel.TEMPORARY
}
