package com.example.slotanalyzer.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DiffDto(
    val fromVersion: Int,
    val toVersion: Int,
    val added: List<DiffMachineItemDto> = emptyList(),
    val updated: List<DiffMachineItemDto> = emptyList(),
    val deleted: List<String> = emptyList()
)

@Serializable
data class DiffMachineItemDto(
    val id: String,
    val name: String? = null,
    val status: String? = null
)
