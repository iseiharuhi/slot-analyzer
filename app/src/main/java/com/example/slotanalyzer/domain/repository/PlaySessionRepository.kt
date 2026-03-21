package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand

interface PlaySessionRepository {
    suspend fun startSession(machineId: String)
    suspend fun getCurrentSession(): PlaySession
    suspend fun updateCounter(command: UpdateCounterCommand)

    suspend fun resetCurrentSessionCounters()
}