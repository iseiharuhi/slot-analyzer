package com.example.slotanalyzer.feature.history.domain.usecase

import com.example.slotanalyzer.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class ObservePlayHistoriesUseCase @Inject constructor(
    private val repository: PlayHistoryRepository
) {
    operator fun invoke() = repository.observeHistories()
}