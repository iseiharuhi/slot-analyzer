package com.example.slotanalyzer.feature.history.detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.inference.domain.model.SettingScore
import com.example.slotanalyzer.feature.history.domain.usecase.GetPlayHistoryDetailUseCase
import com.example.slotanalyzer.core.ui.model.ScoreBarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlayHistoryDetailUseCase: GetPlayHistoryDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    private val historyId: String = savedStateHandle.get<String>("historyId") ?: ""

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            val history = getPlayHistoryDetailUseCase(historyId)

            if (history == null) {
                _uiState.value = HistoryDetailUiState(
                    isLoading = false,
                    summary = "データが見つかりません"
                )
                return@launch
            }

            val settingScores = history.settingScores
            val topSetting = settingScores.maxByOrNull { it.normalizedValue }

            _uiState.value = HistoryDetailUiState(
                machineName = history.machineNameSnapshot,
                playedAtText = formatDate(history.playedDate),
                totalGames = history.totalGames,
                bigRateText = calculateRate(history.totalGames, history.bigCount),
                regRateText = calculateRate(history.totalGames, history.regCount),
                czCount = history.czCount,
                atCount = history.atCount,
                summary = history.inferenceSummary,
                confidenceText = toConfidenceText(history.confidenceLabel.name),
                topSettingText = topSetting?.let {
                    "最有力: 設定${it.setting} (${(it.normalizedValue * 100).toInt()}%)"
                } ?: "",
                settingBars = buildSettingBars(settingScores),
                bandBars = buildBandBars(settingScores),
                isLoading = false
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
}