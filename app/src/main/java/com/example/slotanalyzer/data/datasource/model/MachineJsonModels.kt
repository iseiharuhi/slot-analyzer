package com.example.slotanalyzer.data.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class MachineManifestJson(
    val schemaVersion: Int,
    val masterVersion: String,
    val machines: List<ManifestMachineItem>
)

@Serializable
data class ManifestMachineItem(
    val machineId: String,
    val fileName: String,
    val checksum: String = ""
)

@Serializable
data class MachineMasterJson(
    val machine: MachineJson,
    val counterDefinitions: List<CounterDefinitionJson>,
    val settingReferenceValues: List<SettingReferenceGroupJson>,
    val ceilingRules: List<CeilingRuleJson>
)

@Serializable
data class MachineJson(
    val id: String,
    val name: String,
    val manufacturer: String? = null,
    val type: String? = null,
    val releaseDate: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null
)

@Serializable
data class CounterDefinitionJson(
    val key: String,
    val displayName: String,
    val category: String,
    val sortOrder: Int,
    val unit: String,
    val inputType: String,
    val isEnabled: Boolean,
    val isDefaultVisible: Boolean,
    val supportsMinus: Boolean,
    val notes: String? = null
)

@Serializable
data class SettingReferenceGroupJson(
    val counterKey: String,
    val values: List<SettingReferenceJson>
)

@Serializable
data class SettingReferenceJson(
    val settingNo: Int,
    val denominatorValue: Double? = null,
    val weight: Double,
    val minSampleSize: Int? = null,
    val note: String? = null
)

@Serializable
data class CeilingRuleJson(
    val ruleKey: String,
    val displayName: String,
    val ceilingType: String,
    val limitValue: Int,
    val unit: String,
    val resetOnHit: Boolean,
    val requiresResetFlag: Boolean,
    val description: String? = null,
    val isEnabled: Boolean
)
