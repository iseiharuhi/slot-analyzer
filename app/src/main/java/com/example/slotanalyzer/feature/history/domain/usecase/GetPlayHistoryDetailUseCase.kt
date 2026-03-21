package com.example.slotanalyzer.feature.history.domain.usecase

import com.example.slotanalyzer.feature.history.domain.model.PlayHistory
import com.example.slotanalyzer.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class GetPlayHistoryDetailUseCase @Inject constructor(
    private val repository: PlayHistoryRepository
) {
    suspend operator fun invoke(historyId: String): PlayHistory? {
        return repository.getHistory(historyId)
    }
}