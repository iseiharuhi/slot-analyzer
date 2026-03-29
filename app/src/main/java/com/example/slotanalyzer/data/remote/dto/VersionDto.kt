package com.example.slotanalyzer.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VersionDto(
    val version: Int,
    val oldestAvailableDiffVersion: Int
)
