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
import com.example.slotanalyzer.feature.machine.domain.usecase.GetMachineUseCase
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.usecase.GetCurrentSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class InferenceViewModel @Inject constructor(
    private val getCurrentSessionUseCase: GetCurrentSessionUseCase,
    private val getMachineUseCase: GetMachineUseCase,
    private val calculateInferenceUseCase: CalculateInferenceUseCase,
    private val savePlayHistoryUseCase: SavePlayHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InferenceUiState())
    val uiState: StateFlow<InferenceUiState> = _uiState.asStateFlow()

    private var currentSession: PlaySession? = null
    private var currentInferenceResult: InferenceResult? = null

    init {
        loadInference()
    }

    private fun loadInference() {
        viewModelScope.launch {
            val session = getCurrentSessionUseCase()
            val machine = getMachineUseCase(session.machineId)

            currentSession = session

            val result = calculateInferenceUseCase(
                machine = machine,
                session = session
            )
            currentInferenceResult = result

            val settingBars = buildSettingBars(result.settingScores)
            val bandBars = buildBandBars(result.settingScores)
            val topSetting = result.settingScores.maxByOrNull { it.normalizedValue }

            _uiState.value = InferenceUiState(
                machineName = machine?.name ?: session.machineNameSnapshot,
                inputItems = buildInputItems(
                    machine = machine,
                    session = session
                ),
                summary = result.summary,
                confidenceText = toConfidenceText(result.confidenceLabel.name),
                topSettingText = topSetting?.let {
                    "最有力: 設定${it.setting} (${(it.normalizedValue * 100).toInt()}%)"
                } ?: "",
                settingBars = settingBars,
                bandBars = bandBars
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
                saveMessage = null
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

    private fun buildInputItems(
        machine: Machine?,
        session: PlaySession
    ): List<InferenceInputItemUiModel> {
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
                if (rateText == "--") {
                    "$count ${definition.unit}"
                } else {
                    "$count ${definition.unit} / 確率 $rateText"
                }
            }

            InferenceInputItemUiModel(
                label = definition.displayName,
                valueText = valueText
            )
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
        val low = settingScores
            .filter { it.setting in 1..2 }
            .sumOf { it.normalizedValue }

        val middle = settingScores
            .filter { it.setting in 3..4 }
            .sumOf { it.normalizedValue }

        val high = settingScores
            .filter { it.setting in 5..6 }
            .sumOf { it.normalizedValue }

        return listOf(
            ScoreBarItem(
                label = "低設定帯",
                valueText = "${(low * 100).toInt()}%",
                progress = low.toFloat().coerceIn(0f, 1f)
            ),
            ScoreBarItem(
                label = "中間設定帯",
                valueText = "${(middle * 100).toInt()}%",
                progress = middle.toFloat().coerceIn(0f, 1f)
            ),
            ScoreBarItem(
                label = "高設定帯",
                valueText = "${(high * 100).toInt()}%",
                progress = high.toFloat().coerceIn(0f, 1f)
            )
        )
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