package com.example.slotanalyzer.data.remote.dto

data class MachinesIndexDto(
    val machines: List<MachineSummaryDto>
)

data class MachineSummaryDto(
    val id: String,
    val name: String
)