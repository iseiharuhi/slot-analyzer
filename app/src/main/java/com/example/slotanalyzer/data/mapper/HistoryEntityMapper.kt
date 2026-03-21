package com.example.slotanalyzer.data.mapper

import com.example.slotanalyzer.data.database.entity.PlayHistoryEntity
import com.example.slotanalyzer.feature.history.domain.model.PlayHistory
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import javax.inject.Inject

class HistoryEntityMapper @Inject constructor() {

    fun toDomain(entity: PlayHistoryEntity): PlayHistory =
        PlayHistory(
            id = entity.id,
            sessionId = entity.sessionId,
            machineId = entity.machineId,
            machineNameSnapshot = entity.machineNameSnapshot,
            playedDate = entity.playedDate,
            totalGames = entity.totalGames,
            bigCount = entity.bigCount,
            regCount = entity.regCount,
            czCount = entity.czCount,
            atCount = entity.atCount,
            inferenceSummary = entity.inferenceSummary,
            confidenceLabel = entity.confidenceLabel.toConfidenceLabel(),
            settingScores = decodeSettingScores(entity.settingScores),
            memo = entity.memo,
            createdAt = entity.createdAt
        )

    fun toEntity(domain: PlayHistory): PlayHistoryEntity =
        PlayHistoryEntity(
            id = domain.id,
            sessionId = domain.sessionId,
            machineId = domain.machineId,
            machineNameSnapshot = domain.machineNameSnapshot,
            playedDate = domain.playedDate,
            totalGames = domain.totalGames,
            bigCount = domain.bigCount,
            regCount = domain.regCount,
            czCount = domain.czCount,
            atCount = domain.atCount,
            inferenceSummary = domain.inferenceSummary,
            confidenceLabel = domain.confidenceLabel.name,
            settingScores = encodeSettingScores(domain.settingScores),
            memo = domain.memo,
            createdAt = domain.createdAt
        )

    private fun encodeSettingScores(scores: List<SettingScore>): String {
        if (scores.isEmpty()) return ""
        return scores.joinToString(separator = "|") { score ->
            listOf(
                score.setting.toString(),
                score.rawValue.toString(),
                score.normalizedValue.toString()
            ).joinToString(separator = ",")
        }
    }

    private fun decodeSettingScores(value: String): List<SettingScore> {
        if (value.isBlank()) return emptyList()

        return value.split("|").mapNotNull { item ->
            val parts = item.split(",")
            if (parts.size != 3) return@mapNotNull null

            val setting = parts[0].toIntOrNull() ?: return@mapNotNull null
            val rawValue = parts[1].toDoubleOrNull() ?: return@mapNotNull null
            val normalizedValue = parts[2].toDoubleOrNull() ?: return@mapNotNull null

            SettingScore(
                setting = setting,
                rawValue = rawValue,
                normalizedValue = normalizedValue
            )
        }
    }
}