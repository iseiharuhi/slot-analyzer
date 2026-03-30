package com.example.slotanalyzer.feature.machine.domain.model

data class Machine(
    val id: String,
    val name: String,
    val manufacturer: String?,
    val type: String?,
    val releaseDate: String?,
    val isActive: Boolean,
    val notes: String?,
    val dmmUrl: String? = null,
    val ichigekiUrl: String? = null,
    val counters: List<MachineCounterDefinition>,
    val settingReferenceValues: List<SettingReferenceValue>,
    val ceilingRules: List<CeilingRule>
)
