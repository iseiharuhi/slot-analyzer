package com.example.slotanalyzer.feature.home.domain.usecase

import com.example.slotanalyzer.domain.repository.MachineRepository
import javax.inject.Inject

class SeedMachineMasterUseCase @Inject constructor(
    private val repository: MachineRepository
) {
    suspend operator fun invoke() = repository.seedIfNeeded()
}
