package com.example.slotanalyzer.feature.inference.domain.usecase

import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.feature.inference.domain.model.InferenceResult
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.model.SettingReferenceValue
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import javax.inject.Inject
import kotlin.math.abs

class CalculateInferenceUseCase @Inject constructor() {

    operator fun invoke(
        machine: Machine?,
        session: PlaySession?
    ): InferenceResult {
        if (machine == null || session == null) {
            return emptyResult("機種情報またはセッションが取得できません")
        }

        val totalGames = getCounter(session, "total_games")
        if (totalGames <= 0) {
            return emptyResult("総ゲーム数が未入力のため推測できません")
        }

        val groupedReferences = machine.settingReferenceValues
            .filter { it.denominatorValue != null }
            .groupBy { it.counterKey }

        if (groupedReferences.isEmpty()) {
            return emptyResult("設定判別データが未登録です")
        }

        val rawScores = (1..6).associateWith { 0.001 }.toMutableMap()
        var contributingItems = 0
        val itemSummaries = mutableListOf<String>()

        groupedReferences.forEach { (counterKey, refs) ->
            val count = getCounter(session, counterKey)
            val displayName = machine.counters.firstOrNull { it.key == counterKey }?.displayName ?: counterKey
            val observedRateText = calculateRateText(totalGames, count)
            val minSample = refs.mapNotNull { it.minSampleSize }.maxOrNull() ?: 0

            if (count <= 0 || count < minSample) {
                itemSummaries += if (count <= 0) {
                    "${displayName}: サンプル0のため判定保留"
                } else {
                    "${displayName}: サンプル不足のため判定保留"
                }
                return@forEach
            }

            val observedProbability = count.toDouble() / totalGames.toDouble()

            val perSettingScores = refs.associate { ref ->
                val expectedProbability = toExpectedProbability(ref)
                val score = calculateClosenessScore(
                    observedProbability = observedProbability,
                    expectedProbability = expectedProbability,
                    weight = ref.weight
                )
                ref.settingNo to score
            }

            perSettingScores.forEach { (settingNo, score) ->
                rawScores[settingNo] = (rawScores[settingNo] ?: 0.001) + score
            }

            val bestSetting = perSettingScores.maxByOrNull { it.value }?.key ?: 1
            itemSummaries += "${displayName}: ${observedRateText} → 設定${bestSetting}寄り"
            contributingItems++
        }

        val totalScore = rawScores.values.sum().coerceAtLeast(0.000001)
        val settingScores = (1..6).map { setting ->
            val raw = rawScores[setting] ?: 0.001
            SettingScore(
                setting = setting,
                rawValue = raw,
                normalizedValue = raw / totalScore
            )
        }

        val best = settingScores.maxByOrNull { it.normalizedValue } ?: settingScores.first()

        val overallConfidence = when {
            contributingItems == 0 -> ConfidenceLabel.INSUFFICIENT
            totalGames >= 4000 && best.normalizedValue >= 0.30 -> ConfidenceLabel.HIGH
            totalGames >= 2000 && best.normalizedValue >= 0.24 -> ConfidenceLabel.MEDIUM
            else -> ConfidenceLabel.LOW
        }

        val summary = buildString {
            append("${machine.name} / ")
            append("総ゲーム数${totalGames}G時点で ")
            append("最有力は設定${best.setting} ")
            append("(${(best.normalizedValue * 100.0).toInt()}%)")
            if (itemSummaries.isNotEmpty()) {
                append("\n")
                append(itemSummaries.joinToString(separator = "\n"))
            }
        }

        return InferenceResult(
            summary = summary,
            confidenceLabel = overallConfidence,
            settingScores = settingScores
        )
    }

    private fun getCounter(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    private fun toExpectedProbability(ref: SettingReferenceValue): Double {
        val denominator = ref.denominatorValue ?: return 0.0
        if (denominator <= 0.0) return 0.0
        return 1.0 / denominator
    }

    private fun calculateClosenessScore(
        observedProbability: Double,
        expectedProbability: Double,
        weight: Double
    ): Double {
        if (observedProbability <= 0.0 || expectedProbability <= 0.0) return 0.001

        val diffRatio = abs(observedProbability - expectedProbability) / expectedProbability
        val closeness = 1.0 / (1.0 + diffRatio * 3.0)
        return (closeness * weight.coerceAtLeast(0.1)).coerceAtLeast(0.001)
    }

    private fun calculateRateText(totalGames: Int, count: Int): String {
        if (totalGames <= 0 || count <= 0) return "--"
        return "1/${"%.1f".format(totalGames.toDouble() / count.toDouble())}"
    }

    private fun emptyResult(message: String): InferenceResult {
        val scores = (1..6).map {
            SettingScore(
                setting = it,
                rawValue = 1.0,
                normalizedValue = 1.0 / 6.0
            )
        }

        return InferenceResult(
            summary = message,
            confidenceLabel = ConfidenceLabel.INSUFFICIENT,
            settingScores = scores
        )
    }
}