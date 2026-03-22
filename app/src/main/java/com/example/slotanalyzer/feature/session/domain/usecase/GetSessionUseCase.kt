package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    suspend operator fun invoke(sessionId: String) = repository.getSession(sessionId)
}
