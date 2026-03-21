package com.example.slotanalyzer.feature.history.domain.usecase

import com.example.slotanalyzer.feature.history.domain.model.PlayHistory
import com.example.slotanalyzer.feature.inference.domain.model.InferenceResult
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.domain.repository.PlayHistoryRepository
import java.util.UUID
import javax.inject.Inject

class SavePlayHistoryUseCase @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) {
    suspend operator fun invoke(
        session: PlaySession,
        inferenceResult: InferenceResult
    ) {
        val now = System.currentTimeMillis()

        val totalGames = session.counters.firstOrNull { it.counterKey == "total_games" }?.intValue ?: 0
        val bigCount = session.counters.firstOrNull { it.counterKey == "big_count" }?.intValue ?: 0
        val regCount = session.counters.firstOrNull { it.counterKey == "reg_count" }?.intValue ?: 0
        val czCount = session.counters.firstOrNull { it.counterKey == "cz_count" }?.intValue ?: 0
        val atCount = session.counters.firstOrNull { it.counterKey == "at_count" }?.intValue ?: 0

        val history = PlayHistory(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            machineId = session.machineId,
            machineNameSnapshot = session.machineNameSnapshot,
            playedDate = session.updatedAt,
            totalGames = totalGames,
            bigCount = bigCount,
            regCount = regCount,
            czCount = czCount,
            atCount = atCount,
            inferenceSummary = inferenceResult.summary,
            confidenceLabel = inferenceResult.confidenceLabel,
            settingScores = inferenceResult.settingScores,
            memo = session.memo,
            createdAt = now
        )

        playHistoryRepository.saveHistory(history)
    }
}