package com.example.slotanalyzer.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.database.entity.SessionCounterValueEntity

data class SessionWithCounters(
    @Embedded val session: PlaySessionEntity,
    @Relation(parentColumn = "id", entityColumn = "session_id")
    val counterValues: List<SessionCounterValueEntity>
)
