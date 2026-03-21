package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class GetCurrentSessionUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    suspend operator fun invoke() = repository.getCurrentSession()
}
