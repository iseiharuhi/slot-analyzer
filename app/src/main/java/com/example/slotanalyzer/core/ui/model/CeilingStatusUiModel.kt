package com.example.slotanalyzer.core.ui.model

data class CeilingStatusUiModel(
    val title: String,
    val currentText: String,
    val limitText: String,
    val remainText: String,
    val note: String? = null,
    val isPrimary: Boolean = false,
    val isHighlighted: Boolean = false
)