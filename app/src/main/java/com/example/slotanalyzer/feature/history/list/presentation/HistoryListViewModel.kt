package com.example.slotanalyzer.feature.history.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.history.domain.usecase.ObservePlayHistoriesUseCase
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
    observePlayHistoriesUseCase: ObservePlayHistoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryListUiState())
    val uiState: StateFlow<HistoryListUiState> = _uiState.asStateFlow()

    init {
        observePlayHistoriesUseCase()
            .onEach { histories ->
                _uiState.value = HistoryListUiState(
                    isLoading = false,
                    histories = histories.map { history ->
                        HistoryListItemUiModel(
                            id = history.id,
                            machineName = history.machineNameSnapshot,
                            playedDate = formatDate(history.playedDate),
                            totalGamesText = "${history.totalGames}G",
                            summary = history.inferenceSummary,
                            confidenceLabel = history.confidenceLabel.name
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(time))
    }
}