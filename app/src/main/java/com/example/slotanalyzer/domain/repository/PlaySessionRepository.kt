package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand

interface PlaySessionRepository {
    suspend fun startSession(machineId: String): String
    suspend fun getCurrentSession(): PlaySession
    suspend fun getSession(sessionId: String): PlaySession
    suspend fun getOrCreateSessionForMachine(machineId: String): String
    suspend fun setCurrentSession(sessionId: String)
    suspend fun updateCounter(command: UpdateCounterCommand)
    suspend fun resetSessionCounters(sessionId: String)
}
