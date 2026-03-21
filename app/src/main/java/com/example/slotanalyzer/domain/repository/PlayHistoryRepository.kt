package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.feature.history.domain.model.PlayHistory
import kotlinx.coroutines.flow.Flow

interface PlayHistoryRepository {
    fun observeHistories(): Flow<List<PlayHistory>>
    suspend fun getHistory(historyId: String): PlayHistory?
    suspend fun saveHistory(history: PlayHistory)
    suspend fun deleteHistory(historyId: String)
    suspend fun deleteHistories(historyIds: List<String>)
}