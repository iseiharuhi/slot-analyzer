package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class StartPlaySessionUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    suspend operator fun invoke(machineId: String): String {
        return repository.getOrCreateSessionForMachine(machineId)
    }

    suspend fun startNewSession(machineId: String): String {
        return repository.startSession(machineId)
    }
}
