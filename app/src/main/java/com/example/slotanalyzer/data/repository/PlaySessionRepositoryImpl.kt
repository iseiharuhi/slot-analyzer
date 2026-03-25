package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.PlaySessionDao
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.mapper.SessionEntityMapper
import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.domain.repository.MachineRepository
import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaySessionRepositoryImpl @Inject constructor(
    private val dao: PlaySessionDao,
    private val mapper: SessionEntityMapper,
    private val machineRepository: MachineRepository
) : PlaySessionRepository {

    override suspend fun startSession(machineId: String): String {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()

        val machine = requireNotNull(machineRepository.getMachine(machineId)) {
            "Machine not found for machineId=$machineId"
        }

        dao.clearCurrentFlags()
        dao.insertSession(
            PlaySessionEntity(
                id = sessionId,
                machineId = machine.id,
                machineNameSnapshot = machine.name,
                startedAt = now,
                updatedAt = now,
                endedAt = null,
                isCurrent = true,
                isFinished = false,
                isArchivedInHistory = false
            )
        )

        dao.upsertCounters(
            machine.counters
                .filter { it.isEnabled }
                .map { counterDefinition ->
                    mapper.toCounterEntity(
                        sessionId = sessionId,
                        key = counterDefinition.key,
                        value = 0,
                        now = now
                    )
                }
        )

        return sessionId
    }

    override suspend fun getCurrentSession(): PlaySession {
        val existing = dao.getCurrentSession()
        if (existing != null) return mapper.toDomain(existing)

        val fallbackSessionId = startSession("sample_machine_a")
        return getSession(fallbackSessionId)
    }

    override suspend fun getSession(sessionId: String): PlaySession {
        val session = requireNotNull(dao.getSessionById(sessionId)) {
            "Session not found for sessionId=$sessionId"
        }
        return mapper.toDomain(session)
    }

    override fun observeFinishedSessions(): Flow<List<PlaySession>> {
        return dao.observeFinishedSessions().map { sessions ->
            sessions.map(mapper::toDomain)
        }
    }

    override suspend fun getOrCreateSessionForMachine(machineId: String): String {
        val now = System.currentTimeMillis()
        val existing = dao.getLatestSessionByMachineId(machineId)
        return if (existing != null && !existing.session.isFinished) {
            val sessionId = existing.session.id
            dao.clearCurrentFlags()
            dao.markCurrentSession(sessionId, now)
            sessionId
        } else {
            startSession(machineId)
        }
    }

    override suspend fun setCurrentSession(sessionId: String) {
        val now = System.currentTimeMillis()
        dao.clearCurrentFlags()
        dao.markCurrentSession(sessionId, now)
    }

    override suspend fun updateCounter(command: UpdateCounterCommand) {
        dao.upsertCounter(
            mapper.toCounterEntity(
                sessionId = command.sessionId,
                key = command.counterKey,
                value = command.intValue,
                now = command.updatedAt
            )
        )
        dao.updateSessionUpdatedAt(command.sessionId, command.updatedAt)
    }

    override suspend fun updateInferenceSnapshot(
        sessionId: String,
        summary: String,
        confidenceLabel: ConfidenceLabel?
    ) {
        dao.updateInferenceSnapshot(
            sessionId = sessionId,
            summary = summary,
            confidenceLabel = confidenceLabel?.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun resetSessionCounters(sessionId: String) {
        val now = System.currentTimeMillis()
        val currentSession = getSession(sessionId)

        dao.upsertCounters(
            currentSession.counters.map { counter ->
                mapper.toCounterEntity(
                    sessionId = currentSession.id,
                    key = counter.counterKey,
                    value = if (counter.counterKey.startsWith("ceiling::")) null else 0,
                    now = now
                )
            }
        )

        dao.updateSessionUpdatedAt(currentSession.id, now)
    }

    override suspend fun finishSession(sessionId: String) {
        val now = System.currentTimeMillis()
        dao.finishSession(
            sessionId = sessionId,
            endedAt = now,
            updatedAt = now
        )
    }

    override suspend fun reopenSession(sessionId: String) {
        val now = System.currentTimeMillis()
        dao.clearCurrentFlags()
        dao.reopenSession(
            sessionId = sessionId,
            updatedAt = now
        )
    }
}
