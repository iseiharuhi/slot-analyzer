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
    @Query("SELECT * FROM play_sessions LIMIT 1")
    suspend fun getAnySession(): SessionWithCounters?

    @Query("SELECT * FROM play_sessions LIMIT 1")
    suspend fun getAnySessionEntity(): PlaySessionEntity?

    @Query("DELETE FROM session_counter_values")
    suspend fun deleteAllCounterValues()

    @Query("DELETE FROM play_sessions")
    suspend fun deleteAllSessions()

    @Query("UPDATE play_sessions SET updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionUpdatedAt(sessionId: String, updatedAt: Long)
}