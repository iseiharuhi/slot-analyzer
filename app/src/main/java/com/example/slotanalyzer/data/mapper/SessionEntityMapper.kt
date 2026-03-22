package com.example.slotanalyzer.data.mapper

import com.example.slotanalyzer.data.database.entity.SessionCounterValueEntity
import com.example.slotanalyzer.data.database.relation.SessionWithCounters
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.SessionCounterValue
import javax.inject.Inject

class SessionEntityMapper @Inject constructor() {

    fun toDomain(source: SessionWithCounters): PlaySession =
        PlaySession(
            id = source.session.id,
            machineId = source.session.machineId,
            machineNameSnapshot = source.session.machineNameSnapshot,
            title = source.session.title,
            startedAt = source.session.startedAt,
            updatedAt = source.session.updatedAt,
            endedAt = source.session.endedAt,
            memo = source.session.memo,
            isFinished = source.session.isFinished,
            isCurrent = source.session.isCurrent,
            isArchivedInHistory = source.session.isArchivedInHistory,
            lastInferenceSummary = source.session.lastInferenceSummary,
            lastConfidenceLabel = source.session.lastConfidenceLabel?.toConfidenceLabel(),
            counters = source.counterValues.map {
                SessionCounterValue(
                    counterKey = it.counterKey,
                    intValue = it.intValue,
                    doubleValue = it.doubleValue,
                    updatedAt = it.updatedAt
                )
            },
            ceilingStates = emptyList()
        )

    fun toCounterEntity(
        sessionId: String,
        key: String,
        value: Int,
        now: Long
    ): SessionCounterValueEntity =
        SessionCounterValueEntity(
            sessionId = sessionId,
            counterKey = key,
            intValue = value,
            updatedAt = now
        )
}
