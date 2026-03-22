package com.example.slotanalyzer.feature.history.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.usecase.ObserveFinishedSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    observeFinishedSessionsUseCase: ObserveFinishedSessionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryListUiState())
    val uiState: StateFlow<HistoryListUiState> = _uiState.asStateFlow()

    init {
        observeFinishedSessionsUseCase()
            .onEach { sessions ->
                _uiState.value = HistoryListUiState(
                    isLoading = false,
                    histories = sessions.map(::toUiModel)
                )
            }
            .launchIn(viewModelScope)
    }

    private fun toUiModel(session: PlaySession): HistoryListItemUiModel {
        val totalGames = session.counters.firstOrNull { it.counterKey == TOTAL_GAMES_KEY }?.intValue ?: 0
        return HistoryListItemUiModel(
            sessionId = session.id,
            machineName = session.machineNameSnapshot,
            playedDate = formatDate(resolveDisplayTime(session)),
            dateLabel = resolveDateLabel(session),
            totalGamesText = "${totalGames}G",
            summary = session.lastInferenceSummary ?: "推測結果を確認してください",
            confidenceLabel = toConfidenceText(session.lastConfidenceLabel?.name),
            statusLabel = resolveStatusLabel(session)
        )
    }

    private fun resolveDisplayTime(session: PlaySession): Long {
        return when {
            session.isCurrent -> session.updatedAt
            session.endedAt != null -> session.endedAt
            else -> session.updatedAt
        }
    }

    private fun resolveDateLabel(session: PlaySession): String {
        return when {
            session.isCurrent -> "更新日"
            session.isFinished -> "終了日"
            else -> "保存日"
        }
    }

    private fun resolveStatusLabel(session: PlaySession): String {
        return when {
            session.isCurrent -> "実戦中"
            session.isFinished -> "終了"
            else -> "保存済み"
        }
    }

    private fun toConfidenceText(raw: String?): String {
        return when (raw) {
            "HIGH" -> "高"
            "MEDIUM" -> "中"
            "LOW" -> "低"
            "TEMPORARY" -> "暫定"
            "INSUFFICIENT" -> "サンプル不足"
            null -> "未計算"
            else -> raw
        }
    }

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(time))
    }

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
    }
}
