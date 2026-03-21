package com.example.slotanalyzer.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.slotanalyzer.data.database.entity.*

data class MachineWithDefinitions(
    @Embedded val machine: MachineEntity,
    @Relation(parentColumn = "id", entityColumn = "machine_id")
    val counterDefinitions: List<MachineCounterDefinitionEntity>,
    @Relation(parentColumn = "id", entityColumn = "machine_id")
    val settingReferenceValues: List<MachineSettingReferenceValueEntity>,
    @Relation(parentColumn = "id", entityColumn = "machine_id")
    val ceilingRules: List<MachineCeilingRuleEntity>
)
