package com.example.slotanalyzer.feature.inference.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.core.ui.model.ScoreBarItem
import com.example.slotanalyzer.core.util.RateFormatter
import com.example.slotanalyzer.feature.history.domain.usecase.SavePlayHistoryUseCase
import com.example.slotanalyzer.feature.inference.domain.model.InferenceResult
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.inference.domain.usecase.CalculateInferenceUseCase
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.model.SettingReferenceValue
import com.example.slotanalyzer.feature.machine.domain.usecase.GetMachineUseCase
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.usecase.FinishPlaySessionUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.GetSessionUseCase
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
    private val savePlayHistoryUseCase: SavePlayHistoryUseCase,
    private val finishPlaySessionUseCase: FinishPlaySessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InferenceUiState())
    val uiState: StateFlow<InferenceUiState> = _uiState.asStateFlow()

    private var loadedSessionId: String? = null
    private var currentSession: PlaySession? = null
    private var currentInferenceResult: InferenceResult? = null
    private var currentMachine: Machine? = null

    fun loadInference(sessionId: String) {
        if (sessionId.isBlank() || loadedSessionId == sessionId) return
        loadedSessionId = sessionId

        viewModelScope.launch {
            val session = getSessionUseCase(sessionId)
            val machine = getMachineUseCase(session.machineId)

            currentSession = session
            currentMachine = machine

            val result = calculateInferenceUseCase(
                machine = machine,
                session = session
            )
            currentInferenceResult = result

            val settingBars = buildSettingBars(result.settingScores)
            val bandBars = buildBandBars(result.settingScores)
            val topSetting = result.settingScores.maxByOrNull { it.normalizedValue }

            _uiState.value = InferenceUiState(
                sessionId = sessionId,
                machineName = machine?.name ?: session.machineNameSnapshot,
                inputItems = buildInputItems(machine, session),
                reasonItems = buildReasonItems(machine, session, result),
                summary = result.summary,
                confidenceText = toConfidenceText(result.confidenceLabel.name),
                topSettingText = topSetting?.let {
                    "最有力: 設定${it.setting} (${(it.normalizedValue * 100).toInt()}%)"
                } ?: "",
                settingBars = settingBars,
                bandBars = bandBars,
                isFinished = session.isFinished,
                finishedStatusText = buildFinishedStatusText(session)
            )
        }
    }

    fun saveHistory() {
        val session = currentSession ?: return
        val result = currentInferenceResult ?: return

        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                saveMessage = null,
                finishMessage = null
            )

            runCatching {
                savePlayHistoryUseCase(session, result)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = true,
                    saveMessage = "履歴に保存しました"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = false,
                    saveMessage = "履歴保存に失敗しました"
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
                saveMessage = null
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
        val totalGames = getCounter(session, TOTAL_GAMES_KEY)

        val counterDefinitions = machine?.counters
            ?.sortedBy { it.sortOrder }
            ?.filter { it.isEnabled }
            ?: emptyList()

        return counterDefinitions.map { definition ->
            val count = getCounter(session, definition.key)

            val valueText = if (definition.key == TOTAL_GAMES_KEY) {
                "$count ${definition.unit}"
            } else {
                val rateText = RateFormatter.calculateRateText(totalGames, count)
                if (rateText == "--") "$count ${definition.unit}" else "$count ${definition.unit} / 確率 $rateText"
            }

            InferenceInputItemUiModel(
                label = definition.displayName,
                valueText = valueText
            )
        }
    }

    private fun buildReasonItems(
        machine: Machine?,
        session: PlaySession,
        result: InferenceResult
    ): List<InferenceReasonUiModel> {
        val totalGames = getCounter(session, TOTAL_GAMES_KEY)
        val reasons = mutableListOf<InferenceReasonUiModel>()

        reasons += InferenceReasonUiModel(
            label = "サンプル数",
            valueText = "$totalGames G",
            evaluationText = when {
                totalGames >= 5000 -> "十分に回されており、推測材料として強いです"
                totalGames >= 3000 -> "ある程度判断できますが、まだブレは残ります"
                totalGames >= 1000 -> "暫定判断は可能ですが、追加サンプルが欲しいです"
                else -> "サンプル不足で、結果はかなり暫定です"
            }
        )

        if (machine == null || totalGames <= 0) return reasons

        val topSetting = result.settingScores.maxByOrNull { it.normalizedValue }?.setting
        val visibleDefinitions = machine.counters
            .filter { it.isEnabled && it.isDefaultVisible && it.key != TOTAL_GAMES_KEY }
            .sortedBy { it.sortOrder }

        val groupedRefs = machine.settingReferenceValues
            .filter { it.denominatorValue != null }
            .groupBy { it.counterKey }
            .filterValues { refs -> refs.mapNotNull { it.denominatorValue }.distinct().size > 1 }

        visibleDefinitions.forEach { definition ->
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
                diffRatio <= 0.10 -> "かなり近い"
                diffRatio <= 0.20 -> "比較的近い"
                else -> "やや差があります"
            }
            val consistencyText = when {
                topSetting != null && nearest.settingNo == topSetting -> "最有力の設定${topSetting}と整合しています"
                topSetting != null -> "設定${nearest.settingNo}寄りで、最有力の設定${topSetting}とは少しズレています"
                else -> "設定${nearest.settingNo}寄りです"
            }

            reasons += InferenceReasonUiModel(
                label = definition.displayName,
                valueText = "$count ${definition.unit} / 実測 1/${"%.1f".format(observedDenominator)}",
                evaluationText = "設定${nearest.settingNo}の理論値 1/${"%.1f".format(nearestDenominator)} に$closenessText。$consistencyText。"
            )
        }

        return reasons
    }

    private fun buildSettingBars(settingScores: List<SettingScore>): List<ScoreBarItem> {
        return settingScores.sortedBy { it.setting }.map {
            ScoreBarItem(
                label = "設定${it.setting}",
                valueText = "${(it.normalizedValue * 100).toInt()}%",
                progress = it.normalizedValue.toFloat().coerceIn(0f, 1f)
            )
        }
    }

    private fun buildBandBars(settingScores: List<SettingScore>): List<ScoreBarItem> {
        val low = settingScores.filter { it.setting in 1..2 }.sumOf { it.normalizedValue }
        val middle = settingScores.filter { it.setting in 3..4 }.sumOf { it.normalizedValue }
        val high = settingScores.filter { it.setting in 5..6 }.sumOf { it.normalizedValue }

        return listOf(
            ScoreBarItem("低設定帯", "${(low * 100).toInt()}%", low.toFloat().coerceIn(0f, 1f)),
            ScoreBarItem("中間設定帯", "${(middle * 100).toInt()}%", middle.toFloat().coerceIn(0f, 1f)),
            ScoreBarItem("高設定帯", "${(high * 100).toInt()}%", high.toFloat().coerceIn(0f, 1f))
        )
    }

    private fun buildFinishedStatusText(session: PlaySession): String {
        if (!session.isFinished) return "実戦中"
        val endedAt = session.endedAt ?: return "終了済み"
        return "終了済み: ${formatDateTime(endedAt)}"
    }

    private fun formatDateTime(timestamp: Long): String {
        val formatter = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.JAPAN)
        return formatter.format(java.util.Date(timestamp))
    }

    private fun getCounter(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    private fun toConfidenceText(raw: String): String {
        return when (raw) {
            "HIGH" -> "高"
            "MEDIUM" -> "中"
            "LOW" -> "低"
            "INSUFFICIENT" -> "サンプル不足"
            else -> raw
        }
    }

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
    }
}
