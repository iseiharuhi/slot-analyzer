package com.example.slotanalyzer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.slotanalyzer.data.database.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Query("SELECT * FROM play_histories ORDER BY created_at DESC")
    fun observeHistories(): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_histories WHERE id = :historyId LIMIT 1")
    suspend fun getHistoryById(historyId: String): PlayHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PlayHistoryEntity)

    @Query("DELETE FROM play_histories WHERE id = :historyId")
    suspend fun deleteById(historyId: String)

    @Query("DELETE FROM play_histories WHERE id IN (:historyIds)")
    suspend fun deleteByIds(historyIds: List<String>)
}