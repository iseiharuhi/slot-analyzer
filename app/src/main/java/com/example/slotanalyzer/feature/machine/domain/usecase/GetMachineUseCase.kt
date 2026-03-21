package com.example.slotanalyzer.feature.machine.domain.usecase

import com.example.slotanalyzer.domain.repository.MachineRepository
import javax.inject.Inject

class GetMachineUseCase @Inject constructor(
    private val repository: MachineRepository
) {
    suspend operator fun invoke(machineId: String) = repository.getMachine(machineId)
}