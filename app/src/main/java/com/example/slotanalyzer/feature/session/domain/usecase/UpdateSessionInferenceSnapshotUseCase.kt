package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class UpdateSessionInferenceSnapshotUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        summary: String,
        confidenceLabel: ConfidenceLabel?
    ) {
        repository.updateInferenceSnapshot(sessionId, summary, confidenceLabel)
    }
}
