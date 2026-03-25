package com.example.slotanalyzer.domain.usecase

import com.example.slotanalyzer.domain.model.CeilingType
import com.example.slotanalyzer.domain.model.ceiling.CeilingCalculationResult
import com.example.slotanalyzer.domain.model.ceiling.CeilingStatus
import com.example.slotanalyzer.feature.machine.domain.model.CeilingRule
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import javax.inject.Inject
import kotlin.math.max

class CalculateCeilingStatusUseCase @Inject constructor() {
    operator fun invoke(
        machine: Machine?,
        currentInputs: Map<String, Int>
    ): CeilingCalculationResult {
        if (machine == null) {
            return CeilingCalculationResult(statuses = emptyList())
        }

        val enabledRules = machine.ceilingRules
            .filter { it.isEnabled }
            .sortedWith(
                compareBy<CeilingRule> { it.displayOrder }
                    .thenBy { it.limitValue }
            )

        val primaryRuleKey = enabledRules.firstOrNull { it.isPrimary }?.ruleKey
            ?: enabledRules.firstOrNull { it.ceilingType == CeilingType.GAME }?.ruleKey
            ?: enabledRules.firstOrNull { it.ceilingType == CeilingType.CYCLE }?.ruleKey
            ?: enabledRules.firstOrNull { it.ceilingType == CeilingType.COUNT }?.ruleKey
            ?: enabledRules.firstOrNull { it.ceilingType == CeilingType.POINT }?.ruleKey
            ?: enabledRules.firstOrNull()?.ruleKey

        val statuses = enabledRules.map { rule ->
            val currentValue = currentInputs[rule.ruleKey]?.coerceAtLeast(0) ?: 0
            val remainValue = max(rule.limitValue - currentValue, 0)
            val reached = currentValue >= rule.limitValue

            CeilingStatus(
                ruleKey = rule.ruleKey,
                displayName = rule.displayName,
                currentValue = currentValue,
                limitValue = rule.limitValue,
                remainValue = remainValue,
                unit = rule.unit,
                description = rule.description,
                ceilingType = rule.ceilingType,
                isPrimary = rule.ruleKey == primaryRuleKey,
                isHighlighted = reached || rule.isHighlighted
            )
        }

        return CeilingCalculationResult(statuses = statuses)
    }
}
