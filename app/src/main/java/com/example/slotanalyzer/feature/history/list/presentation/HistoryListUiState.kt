package com.example.slotanalyzer.feature.history.list.presentation

data class HistoryListItemUiModel(
    val sessionId: String,
    val machineName: String,
    val playedDate: String,
    val dateLabel: String,
    val totalGamesText: String,
    val summary: String,
    val confidenceLabel: String,
    val statusLabel: String
)

data class HistoryListUiState(
    val isLoading: Boolean = true,
    val histories: List<HistoryListItemUiModel> = emptyList()
)
