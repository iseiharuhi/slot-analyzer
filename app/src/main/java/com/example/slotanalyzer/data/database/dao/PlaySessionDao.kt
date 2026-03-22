package com.example.slotanalyzer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.database.entity.SessionCounterValueEntity
import com.example.slotanalyzer.data.database.relation.SessionWithCounters

@Dao
interface PlaySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(item: PlaySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCounter(item: SessionCounterValueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCounters(items: List<SessionCounterValueEntity>)

    @Transaction
    @Query("SELECT * FROM play_sessions WHERE is_current = 1 ORDER BY updated_at DESC LIMIT 1")
    suspend fun getCurrentSession(): SessionWithCounters?

    @Transaction
    @Query("SELECT * FROM play_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionWithCounters?

    @Transaction
    @Query("SELECT * FROM play_sessions WHERE machine_id = :machineId ORDER BY updated_at DESC LIMIT 1")
    suspend fun getLatestSessionByMachineId(machineId: String): SessionWithCounters?

    @Query("UPDATE play_sessions SET is_current = 0")
    suspend fun clearCurrentFlags()

    @Query("UPDATE play_sessions SET is_current = 1, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun markCurrentSession(sessionId: String, updatedAt: Long)

    @Query("UPDATE play_sessions SET updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionUpdatedAt(sessionId: String, updatedAt: Long)

    @Query("UPDATE play_sessions SET is_finished = 1, is_current = 0, ended_at = :endedAt, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: String, endedAt: Long, updatedAt: Long)
}
