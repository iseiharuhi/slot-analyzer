package com.example.slotanalyzer.feature.machine.domain.usecase

import com.example.slotanalyzer.feature.machine.domain.model.MachineInferenceSpec
import com.example.slotanalyzer.domain.repository.MachineInferenceSpecRepository
import javax.inject.Inject

class GetMachineInferenceSpecUseCase @Inject constructor(
    private val repository: MachineInferenceSpecRepository
) {
    suspend operator fun invoke(machineId: String): MachineInferenceSpec {
        return repository.getByMachineId(machineId)
            ?: repository.getDefaultSpec()
    }
}