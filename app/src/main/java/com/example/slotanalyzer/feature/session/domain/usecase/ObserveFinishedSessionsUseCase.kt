package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class ObserveFinishedSessionsUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    operator fun invoke() = repository.observeFinishedSessions()
}
