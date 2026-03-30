package com.example.slotanalyzer.feature.history.detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.core.ui.model.ScoreBarItem
import com.example.slotanalyzer.core.util.RateFormatter
import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.inference.domain.usecase.CalculateInferenceUseCase
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.usecase.GetMachineUseCase
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.usecase.GetSessionUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.ReopenPlaySessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed interface HistoryDetailEvent {
    data class NavigateToSessionInput(val sessionId: String) : HistoryDetailEvent
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSessionUseCase: GetSessionUseCase,
    private val getMachineUseCase: GetMachineUseCase,
    private val calculateInferenceUseCase: CalculateInferenceUseCase,
    private val reopenPlaySessionUseCase: ReopenPlaySessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HistoryDetailEvent>()
    val event: SharedFlow<HistoryDetailEvent> = _event.asSharedFlow()

    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            runCatching {
                val session = getSessionUseCase(sessionId)
                val machine = getMachineUseCase(session.machineId)
                val result = calculateInferenceUseCase(machine, session)
                val topSetting = result.settingScores.maxByOrNull { it.normalizedValue }

                HistoryDetailUiState(
                    machineName = machine?.name ?: session.machineNameSnapshot,
                    machineTypeText = machine?.type.orEmpty(),
                    playedAtText = formatDate(session.endedAt ?: session.updatedAt),
                    statusText = resolveStatusText(session),
                    inputItems = buildInputItems(machine, session),
                    reasonItems = buildReasonItems(machine, session, result),
                    summary = result.summary,
                    confidenceText = toConfidenceText(result.confidenceLabel.name),
                    topSettingText = topSetting?.let {
                        "最有力: 設定${it.setting} (${(it.normalizedValue * 100).toInt()}%)"
                    } ?: "",
                    settingBars = buildSettingBars(result.settingScores),
                    bandBars = buildBandBars(result.settingScores),
                    isLoading = false
                )
            }.onSuccess {
                _uiState.value = it
            }.onFailure {
                _uiState.value = HistoryDetailUiState(
                    isLoading = false,
                    errorMessage = "履歴詳細の読み込みに失敗しました"
                )
            }
        }
    }

    fun onResumeSessionClick() {
        if (sessionId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                reopenPlaySessionUseCase(sessionId)
            }.onSuccess {
                _event.emit(HistoryDetailEvent.NavigateToSessionInput(sessionId))
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "セッションの再開に失敗しました"
                )
            }
        }
    }

    private fun buildInputItems(machine: Machine?, session: PlaySession): List<HistoryDetailInputItemUiModel> {
        val totalGames = getCounter(session, TOTAL_GAMES_KEY)
        val definitions = machine?.counters
            ?.sortedBy { it.sortOrder }
            ?.filter { it.isEnabled && (it.key == TOTAL_GAMES_KEY || getCounter(session, it.key) > 0) }
            ?: emptyList()

        return definitions.map { definition ->
            val count = getCounter(session, definition.key)
            val unitText = definition.unit.takeIf { it.isNotBlank() } ?: ""
            val valueText = if (definition.key == TOTAL_GAMES_KEY) {
                "$count $unitText".trim()
            } else {
                val rateText = RateFormatter.calculateRateText(totalGames, count)
                if (rateText == "--") "$count $unitText".trim() else "$count $unitText / 確率 $rateText".trim()
            }

            HistoryDetailInputItemUiModel(
                label = definition.displayName,
                valueText = valueText,
                categoryLabel = definition.category.toLabel()
            )
        }
    }

    private fun buildReasonItems(
        machine: Machine?,
        session: PlaySession,
        result: com.example.slotanalyzer.feature.inference.domain.model.InferenceResult
    ): List<HistoryDetailReasonUiModel> {
        val totalGames = getCounter(session, TOTAL_GAMES_KEY)
        val reasons = mutableListOf<HistoryDetailReasonUiModel>()

        reasons += HistoryDetailReasonUiModel(
            label = "サンプル数",
            valueText = "$totalGames G",
            evaluationText = when {
                totalGames >= 5000 -> "十分に回されており、履歴としても比較しやすい内容です。"
                totalGames >= 3000 -> "一定の判別材料はあります。"
                totalGames >= 1000 -> "暫定判断向けのサンプルです。"
                else -> "総ゲーム数が少なく、参考値として見るのが安全です。"
            }
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
                val count = getCounter(session, definition.key)
                val refs = groupedRefs[definition.key].orEmpty()
                if (count <= 0 || refs.isEmpty()) return@forEach

                val observedDenominator = totalGames.toDouble() / count.toDouble()
                val nearest = refs.minByOrNull { ref ->
                    abs(observedDenominator - (ref.denominatorValue ?: observedDenominator))
                } ?: return@forEach

                val nearestDenominator = nearest.denominatorValue ?: observedDenominator
                val diffRatio = if (nearestDenominator <= 0.0) 0.0 else abs(observedDenominator - nearestDenominator) / nearestDenominator
                val closenessText = when {
                    diffRatio <= 0.08 -> "かなり近い数値でした"
                    diffRatio <= 0.18 -> "比較的近い数値でした"
                    else -> "まだ差がありました"
                }
                val consistencyText = when {
                    topSetting != null && nearest.settingNo == topSetting -> "最有力だった設定${topSetting}と整合しています。"
                    topSetting != null -> "単体では設定${nearest.settingNo}寄りで、最有力の設定${topSetting}とは少しズレています。"
                    else -> "単体では設定${nearest.settingNo}寄りです。"
                }

                reasons += HistoryDetailReasonUiModel(
                    label = definition.displayName,
                    valueText = "$count ${definition.unit} / 実測 1/${"%.1f".format(observedDenominator)}",
                    evaluationText = "設定${nearest.settingNo}の理論値 1/${"%.1f".format(nearestDenominator)} に最も近く、$closenessText $consistencyText"
                )
            }

        return reasons
    }

    private fun resolveStatusText(session: PlaySession): String {
        return when {
            session.isFinished -> "終了済みセッション"
            session.isCurrent -> "実戦中セッション"
            else -> "保存済みセッション"
        }
    }

    private fun getCounter(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
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

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(time))
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

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
    }
}
