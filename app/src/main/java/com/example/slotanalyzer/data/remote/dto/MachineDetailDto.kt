package com.example.slotanalyzer.data.remote.dto

data class MachineDetailDto(
    val id: String,
    val name: String,
    val status: String,
    val externalLinks: List<String>?
)
