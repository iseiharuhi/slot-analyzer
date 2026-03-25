package com.example.slotanalyzer.domain.usecase

import com.example.slotanalyzer.domain.model.CeilingType
import com.example.slotanalyzer.domain.model.ceiling.CeilingCalculationResult
import com.example.slotanalyzer.domain.model.ceiling.CeilingStatus
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import javax.inject.Inject
import kotlin.math.max

class CalculateCeilingStatusUseCase @Inject constructor() {
    operator fun invoke(machine: Machine?, currentGameCount: Int): CeilingCalculationResult {
        if (machine == null) {
            return CeilingCalculationResult(statuses = emptyList())
        }

        val normalizedCurrentGameCount = currentGameCount.coerceAtLeast(0)

        val statuses = machine.ceilingRules
            .asSequence()
            .filter { it.isEnabled }
            .filter { it.ceilingType == CeilingType.GAME }
            .map { rule ->
                CeilingStatus(
                    ruleKey = rule.ruleKey,
                    displayName = rule.displayName,
                    currentValue = normalizedCurrentGameCount,
                    limitValue = rule.limitValue,
                    remainValue = max(rule.limitValue - normalizedCurrentGameCount, 0),
                    unit = rule.unit,
                    description = rule.description
                )
            }
            .toList()

        return CeilingCalculationResult(statuses = statuses)
    }
}
