package com.example.slotanalyzer.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MachineDetailDto(
    val id: String,
    val name: String,
    val manufacturer: String? = null,
    val type: String? = null,
    val releaseDate: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val status: String = "verified",
    val externalLinks: List<String> = emptyList(),
    val counterDefinitions: List<CounterDefinitionDto> = emptyList(),
    val settingReferenceValues: List<SettingReferenceGroupDto> = emptyList(),
    val ceilingRules: List<CeilingRuleDto> = emptyList()
)

@Serializable
data class CounterDefinitionDto(
    val key: String,
    val displayName: String,
    val category: String,
    val sortOrder: Int,
    val unit: String,
    val inputType: String,
    val isEnabled: Boolean = true,
    val isDefaultVisible: Boolean = true,
    val supportsMinus: Boolean = false,
    val notes: String? = null
)

@Serializable
data class SettingReferenceGroupDto(
    val counterKey: String,
    val values: List<SettingReferenceDto> = emptyList()
)

@Serializable
data class SettingReferenceDto(
    val settingNo: Int,
    val denominatorValue: Double? = null,
    val numeratorValue: Double? = null,
    val weight: Double = 1.0,
    val minSampleSize: Int? = null,
    val note: String? = null
)

@Serializable
data class CeilingRuleDto(
    val ruleKey: String,
    val displayName: String,
    val ceilingType: String,
    val limitValue: Int,
    val unit: String,
    val resetOnHit: Boolean = true,
    val requiresResetFlag: Boolean = false,
    val description: String? = null,
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false,
    val isHighlighted: Boolean = false,
    val inputMode: String? = null,
    val stepValue: Int? = null,
    val showInput: Boolean = true,
    val benefitText: String? = null,
    val resetText: String? = null
)
