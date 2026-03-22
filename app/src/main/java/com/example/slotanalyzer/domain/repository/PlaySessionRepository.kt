package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import kotlinx.coroutines.flow.Flow

interface PlaySessionRepository {
    suspend fun startSession(machineId: String): String
    suspend fun getCurrentSession(): PlaySession
    suspend fun getSession(sessionId: String): PlaySession
    fun observeFinishedSessions(): Flow<List<PlaySession>>
    suspend fun getOrCreateSessionForMachine(machineId: String): String
    suspend fun setCurrentSession(sessionId: String)
    suspend fun updateCounter(command: UpdateCounterCommand)
    suspend fun updateInferenceSnapshot(
        sessionId: String,
        summary: String,
        confidenceLabel: ConfidenceLabel?
    )
    suspend fun resetSessionCounters(sessionId: String)
    suspend fun finishSession(sessionId: String)
    suspend fun reopenSession(sessionId: String)
}
