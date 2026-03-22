package com.example.slotanalyzer.feature.history.detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.core.ui.model.ScoreBarItem
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.inference.domain.usecase.CalculateInferenceUseCase
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                val totalGames = getCounter(session, TOTAL_GAMES_KEY)

                HistoryDetailUiState(
                    machineName = machine?.name ?: session.machineNameSnapshot,
                    playedAtText = formatDate(session.endedAt ?: session.updatedAt),
                    totalGames = totalGames,
                    bigRateText = calculateRate(totalGames, getCounter(session, BIG_COUNT_KEY)),
                    regRateText = calculateRate(totalGames, getCounter(session, REG_COUNT_KEY)),
                    czCount = getCounter(session, CZ_COUNT_KEY),
                    atCount = getCounter(session, AT_COUNT_KEY),
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
        val low = settingScores.filter { it.setting in 1..2 }.sumOf { it.normalizedValue }
        val middle = settingScores.filter { it.setting in 3..4 }.sumOf { it.normalizedValue }
        val high = settingScores.filter { it.setting in 5..6 }.sumOf { it.normalizedValue }

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

    private fun calculateRate(totalGames: Int, count: Int): String {
        if (totalGames <= 0 || count <= 0) return "--"
        val rate = totalGames.toDouble() / count.toDouble()
        return "1/${"%.1f".format(rate)}"
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

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
        private const val BIG_COUNT_KEY = "big_count"
        private const val REG_COUNT_KEY = "reg_count"
        private const val CZ_COUNT_KEY = "cz_count"
        private const val AT_COUNT_KEY = "at_count"
    }
}
