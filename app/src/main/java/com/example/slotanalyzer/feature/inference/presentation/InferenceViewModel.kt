package com.example.slotanalyzer.feature.inference.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.core.ui.model.CeilingStatusUiModel
import com.example.slotanalyzer.core.ui.model.ScoreBarItem
import com.example.slotanalyzer.core.util.RateFormatter
import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.domain.usecase.CalculateCeilingStatusUseCase
import com.example.slotanalyzer.feature.inference.domain.model.InferenceResult
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.inference.domain.usecase.CalculateInferenceUseCase
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.usecase.GetMachineUseCase
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.usecase.FinishPlaySessionUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.GetSessionUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.UpdateSessionInferenceSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

@HiltViewModel
class InferenceViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val getMachineUseCase: GetMachineUseCase,
    private val calculateInferenceUseCase: CalculateInferenceUseCase,
    private val updateSessionInferenceSnapshotUseCase: UpdateSessionInferenceSnapshotUseCase,
    private val finishPlaySessionUseCase: FinishPlaySessionUseCase,
    private val calculateCeilingStatusUseCase: CalculateCeilingStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InferenceUiState())
    val uiState: StateFlow<InferenceUiState> = _uiState.asStateFlow()

    private var loadedSessionId: String? = null
    private var currentSession: PlaySession? = null

    fun loadInference(sessionId: String) {
        if (sessionId.isBlank() || loadedSessionId == sessionId) return
        loadedSessionId = sessionId

        viewModelScope.launch {
            val session = getSessionUseCase(sessionId)
            val machine = getMachineUseCase(session.machineId)

            currentSession = session

            val result = calculateInferenceUseCase(
                machine = machine,
                session = session
            )

            val currentGameCount = getCounterOrNull(session, CURRENT_GAME_KEY) ?: 0
            val reasonItems = buildReasonItems(machine, session, result)
            val settingBars = buildSettingBars(result.settingScores)
            val bandBars = buildBandBars(result.settingScores)
            val topSetting = result.settingScores.maxByOrNull { it.normalizedValue }

            _uiState.value = InferenceUiState(
                sessionId = sessionId,
                machineName = machine?.name ?: session.machineNameSnapshot,
                machineTypeText = machine?.type.orEmpty(),
                probabilityModeText = "ベイズ尤度ベース推測（${formatSettingSpec(result.settingScores)}）",
                candidateSummaryText = buildCandidateSummary(result.settingScores),
                inputItems = buildInputItems(machine, session),
                currentGameCount = currentGameCount,
                ceilingItems = buildCeilingItems(machine, session),
                reasonItems = reasonItems,
                reasonSummaryText = buildReasonSummaryText(reasonItems),
                summary = result.summary,
                confidenceText = toConfidenceText(result.confidenceLabel.name),
                topSettingText = topSetting?.let {
                    "最有力: 設定${it.setting} (${(it.normalizedValue * 100).toInt()}%)"
                } ?: "",
                settingBars = settingBars,
                bandBars = bandBars,
                settingDistributionPoints = buildSettingDistributionPoints(result.settingScores),
                isFinished = session.isFinished,
                finishedStatusText = buildFinishedStatusText(session)
            )

            runCatching {
                updateSessionInferenceSnapshotUseCase(
                    sessionId = session.id,
                    summary = result.summary,
                    confidenceLabel = result.confidenceLabel
                )
            }
        }
    }

    fun finishSession() {
        val session = currentSession ?: return
        if (session.isFinished || _uiState.value.isFinishing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFinishing = true,
                finishMessage = null,
            )

            runCatching {
                finishPlaySessionUseCase(session.id)
            }.onSuccess {
                val refreshedSession = getSessionUseCase(session.id)
                currentSession = refreshedSession
                _uiState.value = _uiState.value.copy(
                    isFinishing = false,
                    finishCompleted = true,
                    isFinished = refreshedSession.isFinished,
                    finishedStatusText = buildFinishedStatusText(refreshedSession),
                    finishMessage = "実戦を終了しました"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isFinishing = false,
                    finishCompleted = false,
                    finishMessage = "実戦終了に失敗しました"
                )
            }
        }
    }

    private fun buildInputItems(machine: Machine?, session: PlaySession): List<InferenceInputItemUiModel> {
        val totalGames = getCounterOrNull(session, TOTAL_GAMES_KEY) ?: 0

        val counterDefinitions = machine?.counters
            ?.sortedBy { it.sortOrder }
            ?.filter { definition ->
                definition.isEnabled && (
                    definition.key == TOTAL_GAMES_KEY || hasCounterEntry(session, definition.key)
                )
            }
            ?: emptyList()

        return counterDefinitions.map { definition ->
            val count = getCounterOrNull(session, definition.key)
            val unitText = definition.unit.takeIf { it.isNotBlank() } ?: ""
            val valueText = when {
                definition.key == TOTAL_GAMES_KEY -> count?.let { "$it $unitText".trim() } ?: "ー"
                count == null -> "ー"
                count == 0 -> "$count $unitText / 確率 --".trim()
                else -> {
                    val rateText = RateFormatter.calculateRateText(totalGames, count)
                    if (rateText == "--") {
                        "$count $unitText".trim()
                    } else {
                        "$count $unitText / 確率 $rateText".trim()
                    }
                }
            }

            InferenceInputItemUiModel(
                label = definition.displayName,
                valueText = valueText,
                categoryLabel = definition.category.toLabel()
            )
        }
    }

    private fun buildCeilingItems(
        machine: Machine?,
        session: PlaySession
    ): List<CeilingStatusUiModel> {
        val currentInputs: Map<String, Int> =
            session.counters
                .filter { it.counterKey.startsWith("ceiling::") }
                .associate { counter ->
                    counter.counterKey.removePrefix("ceiling::") to (counter.intValue ?: 0)
                }

        return calculateCeilingStatusUseCase(
            machine = machine,
            currentInputs = currentInputs
        ).statuses.map { status ->
            val unitSuffix = status.unit.ifBlank { "G" }
            CeilingStatusUiModel(
                title = status.displayName,
                currentText = "${status.currentValue}$unitSuffix",
                limitText = "${status.limitValue}$unitSuffix",
                remainText = if (status.remainValue <= 0) "到達済み" else "${status.remainValue}$unitSuffix",
                note = status.description,
                isPrimary = status.isPrimary,
                isHighlighted = status.isHighlighted
            )
        }
    }

    private fun buildReasonItems(
        machine: Machine?,
        session: PlaySession,
        result: InferenceResult
    ): List<InferenceReasonUiModel> {
        val totalGames = getCounterOrNull(session, TOTAL_GAMES_KEY) ?: 0
        val reasons = mutableListOf<InferenceReasonUiModel>()

        val sampleLevel = when {
            totalGames >= 5000 -> "strong"
            totalGames >= 3000 -> "positive"
            totalGames >= 1000 -> "neutral"
            else -> "weak"
        }

        reasons += InferenceReasonUiModel(
            label = "サンプル数",
            valueText = "$totalGames G",
            evaluationText = when {
                totalGames >= 5000 -> "十分に回されており、推測材料としてかなり強いです。"
                totalGames >= 3000 -> "一定の判断はしやすく、現時点でも有力な材料です。"
                totalGames >= 1000 -> "暫定判断は可能ですが、追加サンプルがあると精度が上がります。"
                else -> "総ゲーム数が少なく、結論を強く出すにはまだ弱いです。"
            },
            levelLabel = levelLabel(sampleLevel),
            levelKey = sampleLevel
        )

        if (machine == null || totalGames <= 0) return reasons

        val topSetting = result.settingScores.maxByOrNull { it.normalizedValue }?.setting
        val groupedRefs = machine.settingReferenceValues
            .filter { it.denominatorValue != null }
            .groupBy { it.counterKey }
            .filterValues { refs -> refs.mapNotNull { it.denominatorValue }.distinct().size > 1 }

        machine.counters
            .filter { it.isEnabled && it.isDefaultVisible && it.key != TOTAL_GAMES_KEY }
            .sortedBy { it.sortOrder }
            .forEach { definition ->
                val count = getCounterOrNull(session, definition.key)
                val refs = groupedRefs[definition.key].orEmpty()
                if (count == null || count == 0 || refs.isEmpty()) return@forEach

                val observedDenominator = totalGames.toDouble() / count.toDouble()
                val nearest = refs.minByOrNull { ref ->
                    abs(observedDenominator - (ref.denominatorValue ?: observedDenominator))
                } ?: return@forEach

                val nearestDenominator = nearest.denominatorValue ?: observedDenominator
                val diffRatio = if (nearestDenominator <= 0.0) 0.0 else abs(observedDenominator - nearestDenominator) / nearestDenominator
                val alignsTopSetting = topSetting != null && nearest.settingNo == topSetting

                val levelKey = when {
                    alignsTopSetting && diffRatio <= 0.08 -> "strong"
                    alignsTopSetting && diffRatio <= 0.18 -> "positive"
                    !alignsTopSetting && topSetting != null && diffRatio <= 0.18 -> "contradiction"
                    diffRatio >= 0.30 -> "weak"
                    else -> "neutral"
                }

                val closenessText = when {
                    diffRatio <= 0.08 -> "かなり近い数値です"
                    diffRatio <= 0.18 -> "比較的近い数値です"
                    diffRatio <= 0.30 -> "一定の参考になります"
                    else -> "理論値との差が大きめです"
                }

                val consistencyText = when {
                    alignsTopSetting -> "最有力の設定${topSetting}と整合しています。"
                    topSetting != null && diffRatio <= 0.18 -> "単体では設定${nearest.settingNo}寄りで、最有力の設定${topSetting}とは少し矛盾しています。"
                    topSetting != null -> "単体では設定${nearest.settingNo}寄りですが、決め手としてはまだ弱めです。"
                    else -> "単体では設定${nearest.settingNo}寄りです。"
                }

                reasons += InferenceReasonUiModel(
                    label = definition.displayName,
                    valueText = "$count ${definition.unit} / 実測 1/${"%.1f".format(observedDenominator)}",
                    evaluationText = "設定${nearest.settingNo}の理論値 1/${"%.1f".format(nearestDenominator)} に最も近く、$closenessText $consistencyText",
                    levelLabel = levelLabel(levelKey),
                    levelKey = levelKey
                )
            }

        return reasons
    }

    private fun buildReasonSummaryText(items: List<InferenceReasonUiModel>): String {
        if (items.isEmpty()) return ""
        val strong = items.count { it.levelKey == "strong" }
        val positive = items.count { it.levelKey == "positive" }
        val contradiction = items.count { it.levelKey == "contradiction" }
        val weak = items.count { it.levelKey == "weak" }

        val parts = mutableListOf<String>()
        if (strong > 0) parts += "強い根拠 $strong 件"
        if (positive > 0) parts += "追い風 $positive 件"
        if (contradiction > 0) parts += "矛盾 $contradiction 件"
        if (weak > 0) parts += "弱い要素 $weak 件"

        return if (parts.isEmpty()) "参考要素を確認中" else parts.joinToString(" / ")
    }

    private fun buildFinishedStatusText(session: PlaySession): String {
        return when {
            session.isFinished -> "このセッションは終了済みです。履歴から再開できます。"
            session.isCurrent -> "現在の実戦セッションです。入力を続けながら更新できます。"
            else -> "保存済みセッションです。"
        }
    }

    private fun buildSettingBars(settingScores: List<SettingScore>): List<ScoreBarItem> {
        return settingScores
            .sortedBy { it.setting }
            .map {
                ScoreBarItem(
                    label = "設定${it.setting}",
                    valueText = "${(it.normalizedValue * 100).toInt()}%",
                    progress = it.normalizedValue.toFloat().coerceIn(0f, 1f)
                )
            }
    }

    private fun buildBandBars(settingScores: List<SettingScore>): List<ScoreBarItem> {
        val availableSettings = settingScores.map { it.setting }.toSet()
        val low = settingScores.filter { it.setting in 1..2 }.sumOf { it.normalizedValue }
        val middle = settingScores.filter { it.setting in 3..4 }.sumOf { it.normalizedValue }
        val high = settingScores.filter { it.setting in 5..6 }.sumOf { it.normalizedValue }

        return buildList {
            add(
                ScoreBarItem(
                    label = "低設定帯",
                    valueText = "${(low * 100).toInt()}%",
                    progress = low.toFloat().coerceIn(0f, 1f)
                )
            )
            if (availableSettings.any { it in 3..4 }) {
                add(
                    ScoreBarItem(
                        label = "中間設定帯",
                        valueText = "${(middle * 100).toInt()}%",
                        progress = middle.toFloat().coerceIn(0f, 1f)
                    )
                )
            }
            add(
                ScoreBarItem(
                    label = "高設定帯",
                    valueText = "${(high * 100).toInt()}%",
                    progress = high.toFloat().coerceIn(0f, 1f)
                )
            )
        }
    }

    private fun buildSettingDistributionPoints(settingScores: List<SettingScore>): List<SettingDistributionPointUiModel> {
        return settingScores
            .sortedBy { it.setting }
            .map {
                SettingDistributionPointUiModel(
                    label = "設定${it.setting}",
                    value = it.normalizedValue.toFloat().coerceIn(0f, 1f)
                )
            }
    }


    private fun formatSettingSpec(settingScores: List<SettingScore>): String {
        val settings = settingScores.map { it.setting }.distinct().sorted()
        return when {
            settings == listOf(1, 2, 3, 4, 5, 6) -> "設定1〜6"
            settings.isEmpty() -> "設定1〜6"
            else -> "設定" + settings.joinToString("・")
        }
    }

    private fun buildCandidateSummary(settingScores: List<SettingScore>): String {
        return settingScores
            .sortedByDescending { it.normalizedValue }
            .take(3)
            .joinToString(" / ") {
                "設定${it.setting} ${(it.normalizedValue * 100).toInt()}%"
            }
    }

    private fun toConfidenceText(raw: String): String {
        return when (raw) {
            "HIGH" -> "高"
            "MEDIUM" -> "中"
            "LOW" -> "低"
            "TEMPORARY" -> "暫定"
            "INSUFFICIENT" -> "サンプル不足"
            else -> raw
        }
    }

    private fun getCounterOrNull(session: PlaySession, key: String): Int? {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue
    }

    private fun hasCounterEntry(session: PlaySession, key: String): Boolean {
        return session.counters.any { it.counterKey == key }
    }

    private fun CounterCategory.toLabel(): String {
        return when (this) {
            CounterCategory.BASIC -> "基本"
            CounterCategory.BONUS -> "ボーナス"
            CounterCategory.AT_CZ -> "AT / CZ"
            CounterCategory.SMALL_ROLE -> "小役"
            CounterCategory.SPECIAL -> "特殊"
        }
    }

    private fun levelLabel(levelKey: String): String {
        return when (levelKey) {
            "strong" -> "強い"
            "positive" -> "追い風"
            "contradiction" -> "矛盾"
            "weak" -> "弱い"
            else -> "参考"
        }
    }

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
        private const val CURRENT_GAME_KEY = "current_game"
    }
}
