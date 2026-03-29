package com.example.slotanalyzer.data.remote.dto

data class DiffDto(
    val fromVersion: Int,
    val toVersion: Int,
    val added: List<MachineSummaryDto>,
    val updated: List<MachineSummaryDto>,
    val deleted: List<String>
)

data class MachineSummaryDto(
    val id: String,
    val name: String
)
