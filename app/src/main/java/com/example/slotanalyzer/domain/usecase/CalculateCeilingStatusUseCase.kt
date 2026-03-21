package com.example.slotanalyzer.domain.usecase

import com.example.slotanalyzer.domain.model.ceiling.CeilingCalculationResult
import com.example.slotanalyzer.domain.model.ceiling.CeilingStatus
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import javax.inject.Inject

class CalculateCeilingStatusUseCase @Inject constructor() {
    operator fun invoke(machine: Machine?, session: PlaySession?): CeilingCalculationResult {
        return CeilingCalculationResult(
            statuses = listOf(
                CeilingStatus(
                    ruleKey = "bonus_ceiling",
                    displayName = "ボーナス間天井",
                    currentValue = 0,
                    limitValue = 800,
                    remainValue = 800,
                    unit = "G",
                    description = "サンプル"
                )
            )
        )
    }
}
