package com.example.slotanalyzer.data.datasource.mapper

import com.example.slotanalyzer.data.database.entity.MachineCeilingRuleEntity
import com.example.slotanalyzer.data.database.entity.MachineCounterDefinitionEntity
import com.example.slotanalyzer.data.database.entity.MachineEntity
import com.example.slotanalyzer.data.database.entity.MachineSettingReferenceValueEntity
import com.example.slotanalyzer.data.datasource.model.CeilingRuleJson
import com.example.slotanalyzer.data.datasource.model.CounterDefinitionJson
import com.example.slotanalyzer.data.datasource.model.MachineJson
import com.example.slotanalyzer.data.datasource.model.SettingReferenceGroupJson
import javax.inject.Inject

class MachineMasterJsonMapper @Inject constructor() {

    fun toMachineEntity(json: MachineJson, now: Long): MachineEntity =
        MachineEntity(
            id = json.id,
            name = json.name,
            manufacturer = json.manufacturer,
            type = json.type,
            coinUnit = json.coinUnit,
            releaseDate = json.releaseDate,
            isActive = json.isActive,
            notes = json.notes,
            dmmUrl = json.dmmUrl,
            ichigekiUrl = json.ichigekiUrl,
            createdAt = now,
            updatedAt = now
        )

    fun toCounterEntities(
        machineId: String,
        items: List<CounterDefinitionJson>
    ): List<MachineCounterDefinitionEntity> =
        items.map {
            MachineCounterDefinitionEntity(
                machineId = machineId,
                key = it.key,
                displayName = it.displayName,
                category = it.category,
                sortOrder = it.sortOrder,
                unit = it.unit,
                inputType = it.inputType,
                isEnabled = it.isEnabled,
                isDefaultVisible = it.isDefaultVisible,
                supportsMinus = it.supportsMinus,
                notes = it.notes
            )
        }

    fun toReferenceEntities(
        machineId: String,
        items: List<SettingReferenceGroupJson>
    ): List<MachineSettingReferenceValueEntity> =
        items.flatMap { group ->
            group.values.map {
                MachineSettingReferenceValueEntity(
                    machineId = machineId,
                    counterKey = group.counterKey,
                    settingNo = it.settingNo,
                    denominatorValue = it.denominatorValue,
                    weight = it.weight,
                    minSampleSize = it.minSampleSize,
                    note = it.note
                )
            }
        }

    fun toCeilingEntities(
        machineId: String,
        items: List<CeilingRuleJson>
    ): List<MachineCeilingRuleEntity> =
        items.map {
            MachineCeilingRuleEntity(
                machineId = machineId,
                ruleKey = it.ruleKey,
                displayName = it.displayName,
                ceilingType = it.ceilingType,
                limitValue = it.limitValue,
                unit = it.unit,
                resetOnHit = it.resetOnHit,
                requiresResetFlag = it.requiresResetFlag,
                description = it.description,
                isEnabled = it.isEnabled,
                displayOrder = it.displayOrder,
                isPrimary = it.isPrimary,
                isHighlighted = it.isHighlighted,
                inputMode = it.inputMode,
                stepValue = it.stepValue,
                showInput = it.showInput,
                benefitText = it.benefitText,
                resetText = it.resetText
            )
        }
}
