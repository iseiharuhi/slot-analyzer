package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.PlayHistoryDao
import com.example.slotanalyzer.data.mapper.HistoryEntityMapper
import com.example.slotanalyzer.feature.history.domain.model.PlayHistory
import com.example.slotanalyzer.domain.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayHistoryRepositoryImpl @Inject constructor(
    private val dao: PlayHistoryDao,
    private val mapper: HistoryEntityMapper
) : PlayHistoryRepository {

    override fun observeHistories(): Flow<List<PlayHistory>> {
        return dao.observeHistories().map { list -> list.map(mapper::toDomain) }
    }

    override suspend fun getHistory(historyId: String): PlayHistory? {
        return dao.getHistoryById(historyId)?.let(mapper::toDomain)
    }

    override suspend fun saveHistory(history: PlayHistory) {
        dao.insert(mapper.toEntity(history))
    }

    override suspend fun deleteHistory(historyId: String) {
        dao.deleteById(historyId)
    }

    override suspend fun deleteHistories(historyIds: List<String>) {
        if (historyIds.isEmpty()) return
        dao.deleteByIds(historyIds)
    }
}