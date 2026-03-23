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
        session: PlaySession
    ): InferenceResult {
        if (machine == null) {
            return emptyResult("機種情報がありません")
        }

        val totalGames = getCounter(session, TOTAL_GAMES_KEY)
        if (totalGames <= 0) {
            return emptyResult("総回転数が不足しています")
        }

        val groupedReferences = getValidReferences(machine)
        if (groupedReferences.isEmpty()) {
            return emptyResult("設定差データがありません")
        }

        val eligibleCounterKeys = groupedReferences
            .filterValues { refs ->
                val observed = getCounter(session, refs.first().counterKey)
                observed > 0
            }
            .keys

        if (eligibleCounterKeys.isEmpty()) {
            return emptyResult("判別対象の入力がまだ不足しています")
        }

        val scores = (1..6).map { setting ->
            val raw = calculateScore(
                setting = setting,
                refs = groupedReferences,
                session = session,
                totalGames = totalGames
            )

            SettingScore(
                setting = setting,
                rawValue = raw,
                normalizedValue = 0.0
            )
        }

        val normalized = normalize(scores)
        val confidence = calculateConfidence(totalGames, normalized, eligibleCounterKeys.size)
        val summary = buildSummary(normalized, confidence, eligibleCounterKeys.size)

        return InferenceResult(
            settingScores = normalized,
            summary = summary,
            confidenceLabel = confidence
        )
    }

    private fun getValidReferences(machine: Machine): Map<String, List<SettingReferenceValue>> {
        val visibleKeys = machine.counters
            .filter { it.isEnabled && it.isDefaultVisible }
            .map { it.key }
            .toSet()

        return machine.settingReferenceValues
            .filter { it.denominatorValue != null }
            .filter { it.counterKey in visibleKeys }
            .groupBy { it.counterKey }
            .filterValues { refs ->
                refs.mapNotNull { it.denominatorValue }.distinct().size > 1
            }
    }

    private fun calculateScore(
        setting: Int,
        refs: Map<String, List<SettingReferenceValue>>,
        session: PlaySession,
        totalGames: Int
    ): Double {
        var weightedSum = 0.0
        var totalWeight = 0.0

        refs.forEach { (key, list) ->
            val observed = getCounter(session, key)
            if (observed <= 0) return@forEach

            val ref = list.firstOrNull { it.settingNo == setting } ?: return@forEach
            val denom = ref.denominatorValue ?: return@forEach
            if (denom <= 0.0) return@forEach

            val minSampleSize = ref.minSampleSize ?: 1
            val sampleFactor = (observed.toDouble() / minSampleSize.toDouble()).coerceIn(0.25, 1.0)
            val weight = ref.weight.coerceAtLeast(0.1) * sampleFactor
            val expected = totalGames.toDouble() / denom

            val score = calcItemScore(
                observed = observed.toDouble(),
                expected = expected
            )

            weightedSum += score * weight
            totalWeight += weight
        }

        if (totalWeight <= 0.0) return MIN_SCORE
        return weightedSum / totalWeight
    }

    private fun calcItemScore(
        observed: Double,
        expected: Double
    ): Double {
        if (expected <= 0.0) return MIN_SCORE

        val diff = abs(observed - expected)
        val ratio = diff / expected

        return when {
            ratio <= 0.03 -> 1.0
            ratio <= 0.08 -> 0.92
            ratio <= 0.15 -> 0.82
            ratio <= 0.25 -> 0.68
            ratio <= 0.40 -> 0.45
            else -> MIN_SCORE
        }
    }

    private fun normalize(scores: List<SettingScore>): List<SettingScore> {
        val adjusted = scores.map {
            it.copy(rawValue = it.rawValue.coerceAtLeast(MIN_SCORE))
        }

        val total = adjusted.sumOf { it.rawValue }

        if (total <= 0.0) {
            val even = 1.0 / adjusted.size.toDouble()
            return adjusted.map {
                it.copy(normalizedValue = even)
            }
        }

        return adjusted.map {
            it.copy(normalizedValue = it.rawValue / total)
        }
    }

    private fun calculateConfidence(
        totalGames: Int,
        scores: List<SettingScore>,
        observedItemCount: Int
    ): ConfidenceLabel {
        if (totalGames < 1000) return ConfidenceLabel.INSUFFICIENT
        if (observedItemCount <= 1) return ConfidenceLabel.TEMPORARY

        val topTwo = scores
            .sortedByDescending { it.normalizedValue }
            .take(2)

        if (topTwo.size < 2) return ConfidenceLabel.LOW

        val gap = topTwo[0].normalizedValue - topTwo[1].normalizedValue

        return when {
            totalGames >= 5000 && observedItemCount >= 3 && gap >= 0.18 -> ConfidenceLabel.HIGH
            totalGames >= 3000 && observedItemCount >= 2 && gap >= 0.10 -> ConfidenceLabel.MEDIUM
            observedItemCount >= 2 -> ConfidenceLabel.LOW
            else -> ConfidenceLabel.TEMPORARY
        }
    }

    private fun buildSummary(
        scores: List<SettingScore>,
        confidence: ConfidenceLabel,
        observedItemCount: Int
    ): String {
        val sorted = scores.sortedByDescending { it.normalizedValue }
        val top = sorted.firstOrNull() ?: return "不明"
        val second = sorted.getOrNull(1)

        val percent = (top.normalizedValue * 100).toInt()
        val gapPercent = second?.let { ((top.normalizedValue - it.normalizedValue) * 100).toInt() } ?: 0

        val confidenceText = when (confidence) {
            ConfidenceLabel.HIGH -> "十分なサンプルがあり、信頼度は高めです"
            ConfidenceLabel.MEDIUM -> "ある程度のサンプルがあり、信頼度は中程度です"
            ConfidenceLabel.LOW -> "まだブレはありますが、現状では有力です"
            ConfidenceLabel.TEMPORARY -> "入力項目が少なく、現時点では暫定です"
            ConfidenceLabel.INSUFFICIENT -> "総ゲーム数が少なく、サンプル不足です"
        }

        return "設定${top.setting}が最有力（${percent}%）。判別に使えた項目は${observedItemCount}件、次点との差は約${gapPercent}%です。$confidenceText。"
    }

    private fun emptyResult(message: String): InferenceResult {
        val even = 1.0 / 6.0

        return InferenceResult(
            settingScores = (1..6).map { setting ->
                SettingScore(
                    setting = setting,
                    rawValue = MIN_SCORE,
                    normalizedValue = even
                )
            },
            summary = message,
            confidenceLabel = ConfidenceLabel.INSUFFICIENT
        )
    }

    private fun getCounter(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
        private const val MIN_SCORE = 0.0001
    }
}
