package com.example.slotanalyzer.feature.inference.domain.usecase

import com.example.slotanalyzer.domain.model.ConfidenceLabel
import com.example.slotanalyzer.feature.inference.domain.model.InferenceResult
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.model.SettingReferenceValue
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.ln

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

        val evidenceItems = groupedReferences.mapNotNull { (counterKey, refs) ->
            buildEvidenceItem(
                counterKey = counterKey,
                refs = refs,
                session = session,
                totalGames = totalGames
            )
        }

        if (evidenceItems.isEmpty()) {
            return emptyResult("判別対象の入力がまだ不足しています")
        }

        val availableSettings = resolveAvailableSettings(machine, groupedReferences)
        val logScores = availableSettings.associateWith { setting ->
            evidenceItems.sumOf { evidence ->
                val ref = evidence.refsBySetting[setting] ?: return@sumOf 0.0
                val denominator = ref.denominatorValue ?: return@sumOf 0.0
                if (denominator <= 0.0) return@sumOf 0.0

                val lambda = (totalGames.toDouble() / denominator).coerceAtLeast(MIN_LAMBDA)
                poissonLogProbability(evidence.observedCount, lambda) * evidence.effectiveWeight
            }
        }

        val normalized = normalizeFromLogScores(logScores)
        val confidence = calculateConfidence(
            totalGames = totalGames,
            scores = normalized,
            usedEvidenceCount = evidenceItems.size,
            totalEffectiveWeight = evidenceItems.sumOf { it.effectiveWeight }
        )
        val summary = buildSummary(normalized, confidence, evidenceItems)

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

    private fun buildEvidenceItem(
        counterKey: String,
        refs: List<SettingReferenceValue>,
        session: PlaySession,
        totalGames: Int
    ): EvidenceItem? {
        val observedCount = getCounter(session, counterKey)
        val expectedCounts = refs.mapNotNull { ref ->
            ref.denominatorValue?.takeIf { it > 0.0 }?.let { denominator ->
                totalGames.toDouble() / denominator
            }
        }
        if (expectedCounts.isEmpty()) return null

        val averageExpected = expectedCounts.average()
        val strongestExpected = expectedCounts.maxOrNull() ?: 0.0
        val baseWeight = refs.maxOfOrNull { it.weight }?.coerceAtLeast(MIN_WEIGHT) ?: MIN_WEIGHT
        val minRequiredEvents = refs.mapNotNull { it.minSampleSize }.minOrNull()?.coerceAtLeast(1) ?: 1

        val expectedMaturity = (averageExpected / minRequiredEvents.toDouble()).coerceIn(0.0, 1.0)
        val observedMaturity = if (observedCount > 0) {
            (observedCount.toDouble() / minRequiredEvents.toDouble()).coerceIn(0.0, 1.0)
        } else {
            0.0
        }

        val maturity = when {
            observedCount > 0 -> maxOf(expectedMaturity, observedMaturity).coerceIn(0.15, 1.0)
            strongestExpected >= ZERO_EVENT_MIN_EXPECTED -> expectedMaturity.coerceIn(0.10, 0.85)
            else -> 0.0
        }

        if (maturity <= 0.0) return null

        return EvidenceItem(
            counterKey = counterKey,
            observedCount = observedCount,
            effectiveWeight = baseWeight * maturity,
            refsBySetting = refs.associateBy { it.settingNo }
        )
    }

    private fun normalizeFromLogScores(logScores: Map<Int, Double>): List<SettingScore> {
        if (logScores.isEmpty()) {
            val defaultSettings = DEFAULT_SETTINGS
            return defaultSettings.map {
                SettingScore(
                    setting = it,
                    rawValue = MIN_SCORE,
                    normalizedValue = 1.0 / defaultSettings.size.toDouble()
                )
            }
        }

        val maxLog = logScores.values.maxOrNull() ?: 0.0
        val rawScores = logScores.mapValues { (_, value) ->
            kotlin.math.exp(value - maxLog).coerceAtLeast(MIN_SCORE)
        }
        val total = rawScores.values.sum()

        if (total <= 0.0) {
            val even = 1.0 / logScores.size.toDouble()
            return logScores.keys.sorted().map { setting ->
                SettingScore(setting = setting, rawValue = MIN_SCORE, normalizedValue = even)
            }
        }

        return logScores.keys.sorted().map { setting ->
            val raw = rawScores.getValue(setting)
            SettingScore(
                setting = setting,
                rawValue = raw,
                normalizedValue = raw / total
            )
        }
    }

    private fun calculateConfidence(
        totalGames: Int,
        scores: List<SettingScore>,
        usedEvidenceCount: Int,
        totalEffectiveWeight: Double
    ): ConfidenceLabel {
        if (totalGames < 800) return ConfidenceLabel.INSUFFICIENT
        if (usedEvidenceCount == 0) return ConfidenceLabel.INSUFFICIENT

        val sorted = scores.sortedByDescending { it.normalizedValue }
        val top = sorted.firstOrNull()?.normalizedValue ?: return ConfidenceLabel.INSUFFICIENT
        val second = sorted.getOrNull(1)?.normalizedValue ?: 0.0
        val gap = top - second
        val entropy = normalizedEntropy(scores)

        return when {
            totalGames >= 5000 && usedEvidenceCount >= 3 && totalEffectiveWeight >= 2.4 && top >= 0.58 && gap >= 0.20 && entropy <= 0.72 -> ConfidenceLabel.HIGH
            totalGames >= 3000 && usedEvidenceCount >= 2 && totalEffectiveWeight >= 1.5 && top >= 0.42 && gap >= 0.12 && entropy <= 0.84 -> ConfidenceLabel.MEDIUM
            totalGames >= 1500 && usedEvidenceCount >= 2 && totalEffectiveWeight >= 1.0 -> ConfidenceLabel.LOW
            usedEvidenceCount >= 1 -> ConfidenceLabel.TEMPORARY
            else -> ConfidenceLabel.INSUFFICIENT
        }
    }

    private fun buildSummary(
        scores: List<SettingScore>,
        confidence: ConfidenceLabel,
        evidenceItems: List<EvidenceItem>
    ): String {
        val sorted = scores.sortedByDescending { it.normalizedValue }
        val top = sorted.firstOrNull() ?: return "不明"
        val second = sorted.getOrNull(1)
        val settingNos = scores.map { it.setting }.toSet()
        val highBand = scores.filter { it.setting in 5..6 }.sumOf { it.normalizedValue }
        val middleBand = scores.filter { it.setting in 3..4 }.sumOf { it.normalizedValue }
        val lowBand = scores.filter { it.setting in 1..2 }.sumOf { it.normalizedValue }

        val topPercent = (top.normalizedValue * 100).toInt()
        val gapPercent = second?.let { ((top.normalizedValue - it.normalizedValue) * 100).toInt() } ?: 0
        val hasMiddleBand = settingNos.any { it in 3..4 }
        val bandText = when {
            highBand >= 0.55 -> "高設定帯が優勢"
            lowBand >= 0.55 -> "低設定帯が優勢"
            hasMiddleBand && middleBand >= 0.45 -> "中間設定帯が中心"
            else -> "設定帯は拮抗"
        }

        val confidenceText = when (confidence) {
            ConfidenceLabel.HIGH -> "複数要素が同じ方向を向いており、信頼度は高めです"
            ConfidenceLabel.MEDIUM -> "有効な判別要素が揃い始めており、ある程度の信頼がおけます"
            ConfidenceLabel.LOW -> "一定の傾向は出ていますが、まだ逆転余地があります"
            ConfidenceLabel.TEMPORARY -> "有効サンプルが限られるため、現時点では暫定判断です"
            ConfidenceLabel.INSUFFICIENT -> "サンプル不足のため、まだ判断材料が足りません"
        }

        return "設定${top.setting}が最有力（${topPercent}%）。次点との差は約${gapPercent}%、有効判別要素は${evidenceItems.size}件です。${bandText}。${confidenceText}。"
    }

    private fun poissonLogProbability(observed: Int, lambda: Double): Double {
        val safeLambda = lambda.coerceAtLeast(MIN_LAMBDA)
        return observed * ln(safeLambda) - safeLambda - logFactorial(observed)
    }

    private fun logFactorial(value: Int): Double {
        if (value <= 1) return 0.0
        if (value < LOG_FACTORIAL_CACHE.size) return LOG_FACTORIAL_CACHE[value]

        val n = value.toDouble()
        return 0.5 * ln(2.0 * PI * n) + n * ln(n) - n
    }

    private fun normalizedEntropy(scores: List<SettingScore>): Double {
        val probabilities = scores.map { it.normalizedValue.coerceAtLeast(MIN_SCORE) }
        val entropy = -probabilities.sumOf { probability -> probability * ln(probability) }
        return entropy / ln(probabilities.size.toDouble())
    }

    private fun emptyResult(message: String): InferenceResult {
        val defaultSettings = DEFAULT_SETTINGS
        val even = 1.0 / defaultSettings.size.toDouble()

        return InferenceResult(
            settingScores = defaultSettings.map { setting ->
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

    private fun resolveAvailableSettings(
        machine: Machine,
        groupedReferences: Map<String, List<SettingReferenceValue>>
    ): List<Int> {
        val fromReferences = groupedReferences.values
            .flatten()
            .map { it.settingNo }
            .distinct()
            .sorted()

        return if (fromReferences.isNotEmpty()) {
            fromReferences
        } else {
            DEFAULT_SETTINGS
        }
    }

    private fun getCounter(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    private data class EvidenceItem(
        val counterKey: String,
        val observedCount: Int,
        val effectiveWeight: Double,
        val refsBySetting: Map<Int, SettingReferenceValue>
    )

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
        private val DEFAULT_SETTINGS = listOf(1, 2, 3, 4, 5, 6)
        private const val MIN_SCORE = 0.0001
        private const val MIN_LAMBDA = 0.0000001
        private const val MIN_WEIGHT = 0.1
        private const val ZERO_EVENT_MIN_EXPECTED = 0.8

        private val LOG_FACTORIAL_CACHE: DoubleArray = DoubleArray(512).apply {
            this[0] = 0.0
            this[1] = 0.0
            for (index in 2 until size) {
                this[index] = this[index - 1] + ln(index.toDouble())
            }
        }
    }
}
