package com.example.slotanalyzer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.database.entity.SessionCounterValueEntity
import com.example.slotanalyzer.data.database.relation.SessionWithCounters
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT * FROM play_sessions WHERE is_archived_in_history = 1 ORDER BY COALESCE(ended_at, updated_at) DESC")
    fun observeFinishedSessions(): Flow<List<SessionWithCounters>>

    @Transaction
    @Query("SELECT * FROM play_sessions WHERE machine_id = :machineId ORDER BY updated_at DESC LIMIT 1")
    suspend fun getLatestSessionByMachineId(machineId: String): SessionWithCounters?

    @Query("UPDATE play_sessions SET is_current = 0")
    suspend fun clearCurrentFlags()

    @Query("UPDATE play_sessions SET is_current = 1, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun markCurrentSession(sessionId: String, updatedAt: Long)

    @Query("UPDATE play_sessions SET updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionUpdatedAt(sessionId: String, updatedAt: Long)

    @Query("UPDATE play_sessions SET last_inference_summary = :summary, last_confidence_label = :confidenceLabel, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updateInferenceSnapshot(sessionId: String, summary: String, confidenceLabel: String?, updatedAt: Long)

    @Query("UPDATE play_sessions SET is_finished = 1, is_current = 0, is_archived_in_history = 1, ended_at = :endedAt, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: String, endedAt: Long, updatedAt: Long)

    @Query("UPDATE play_sessions SET is_finished = 0, is_current = 1, ended_at = NULL, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun reopenSession(sessionId: String, updatedAt: Long)
}
