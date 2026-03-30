package com.example.slotanalyzer.data.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class MasterVersionJson(
    val version: Int? = null,
    val masterVersion: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class MasterMachineSummaryJson(
    val id: String,
    val name: String,
    val manufacturer: String? = null,
    val type: String? = null,
    val releaseDate: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val detailFileName: String? = null,
    val hasCeiling: Boolean? = null,
    val status: String? = null
)
