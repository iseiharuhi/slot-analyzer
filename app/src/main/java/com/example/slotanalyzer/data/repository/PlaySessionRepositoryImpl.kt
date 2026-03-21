package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.PlaySessionDao
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.mapper.SessionEntityMapper
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import com.example.slotanalyzer.domain.repository.MachineRepository
import com.example.slotanalyzer.domain.repository.PlaySessionRepository
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
            listOf(
                mapper.toCounterEntity(sessionId, "total_games", 0, now),
                mapper.toCounterEntity(sessionId, "big_count", 0, now),
                mapper.toCounterEntity(sessionId, "reg_count", 0, now),
                mapper.toCounterEntity(sessionId, "cz_count", 0, now),
                mapper.toCounterEntity(sessionId, "at_count", 0, now)
            )
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
}