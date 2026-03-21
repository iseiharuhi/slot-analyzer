package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.PlaySessionDao
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.mapper.SessionEntityMapper
import com.example.slotanalyzer.domain.repository.MachineRepository
import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import java.util.UUID
import javax.inject.Inject

class PlaySessionRepositoryImpl @Inject constructor(
    private val dao: PlaySessionDao,
    private val mapper: SessionEntityMapper,
    private val machineRepository: MachineRepository
) : PlaySessionRepository {

    override suspend fun startSession(machineId: String) {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()

        val machine = requireNotNull(machineRepository.getMachine(machineId)) {
            "Machine not found for machineId=$machineId"
        }

        dao.deleteAllCounterValues()
        dao.deleteAllSessions()

        dao.insertSession(
            PlaySessionEntity(
                id = sessionId,
                machineId = machine.id,
                machineNameSnapshot = machine.name,
                startedAt = now,
                updatedAt = now
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
    }

    override suspend fun getCurrentSession(): PlaySession {
        val existing = dao.getAnySession()
        if (existing != null) return mapper.toDomain(existing)

        startSession("sample_machine_a")
        return mapper.toDomain(requireNotNull(dao.getAnySession()))
    }

    override suspend fun updateCounter(command: UpdateCounterCommand) {
        dao.upsertCounter(
            mapper.toCounterEntity(
                sessionId = command.sessionId,
                key = command.counterKey,
                value = command.intValue ?: 0,
                now = command.updatedAt
            )
        )
        dao.updateSessionUpdatedAt(command.sessionId, command.updatedAt)
    }

    override suspend fun resetCurrentSessionCounters() {
        val session = dao.getAnySession() ?: return
        val now = System.currentTimeMillis()
        val currentSession = mapper.toDomain(session)

        dao.upsertCounters(
            currentSession.counters.map { counter ->
                mapper.toCounterEntity(
                    sessionId = currentSession.id,
                    key = counter.counterKey,
                    value = 0,
                    now = now
                )
            }
        )

        dao.updateSessionUpdatedAt(currentSession.id, now)
    }
}