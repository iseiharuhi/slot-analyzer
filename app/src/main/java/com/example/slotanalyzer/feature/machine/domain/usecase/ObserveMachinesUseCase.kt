package com.example.slotanalyzer.feature.machine.domain.usecase

import com.example.slotanalyzer.domain.repository.MachineRepository
import javax.inject.Inject

class ObserveMachinesUseCase @Inject constructor(
    private val repository: MachineRepository
) {
    operator fun invoke() = repository.observeActiveMachines()
}
