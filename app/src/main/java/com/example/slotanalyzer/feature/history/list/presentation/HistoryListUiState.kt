package com.example.slotanalyzer.feature.history.list.presentation

data class HistoryListItemUiModel(
    val id: String,
    val machineName: String,
    val playedDate: String,
    val totalGamesText: String,
    val summary: String,
    val confidenceLabel: String
)

data class HistoryListUiState(
    val isLoading: Boolean = true,
    val histories: List<HistoryListItemUiModel> = emptyList()
)