package com.example.slotanalyzer.feature.history.domain.usecase

import com.example.slotanalyzer.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class DeletePlayHistoriesUseCase @Inject constructor(
    private val repository: PlayHistoryRepository
) {
    suspend operator fun invoke(historyIds: List<String>) {
        repository.deleteHistories(historyIds)
    }
}