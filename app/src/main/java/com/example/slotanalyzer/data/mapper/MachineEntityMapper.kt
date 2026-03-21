package com.example.slotanalyzer.data.mapper

import com.example.slotanalyzer.data.database.entity.*
import com.example.slotanalyzer.data.database.relation.MachineWithDefinitions
import com.example.slotanalyzer.feature.machine.domain.model.*
import javax.inject.Inject

class MachineEntityMapper @Inject constructor() {

    fun toDomain(source: MachineWithDefinitions): Machine =
        Machine(
            id = source.machine.id,
            name = source.machine.name,
            manufacturer = source.machine.manufacturer,
            type = source.machine.type,
            releaseDate = source.machine.releaseDate,
            isActive = source.machine.isActive,
            notes = source.machine.notes,
            counters = source.counterDefinitions.map { toCounterDomain(it) },
            settingReferenceValues = source.settingReferenceValues.map { toReferenceDomain(it) },
            ceilingRules = source.ceilingRules.map { toCeilingDomain(it) }
        )

    private fun toCounterDomain(entity: MachineCounterDefinitionEntity): MachineCounterDefinition =
        MachineCounterDefinition(
            key = entity.key,
            displayName = entity.displayName,
            category = entity.category.toCounterCategory(),
            sortOrder = entity.sortOrder,
            unit = entity.unit,
            inputType = entity.inputType.toCounterInputType(),
            isEnabled = entity.isEnabled,
            isDefaultVisible = entity.isDefaultVisible,
            supportsMinus = entity.supportsMinus,
            notes = entity.notes
        )

    private fun toReferenceDomain(entity: MachineSettingReferenceValueEntity): SettingReferenceValue =
        SettingReferenceValue(
            counterKey = entity.counterKey,
            settingNo = entity.settingNo,
            denominatorValue = entity.denominatorValue,
            numeratorValue = entity.numeratorValue,
            weight = entity.weight,
            minSampleSize = entity.minSampleSize,
            note = entity.note
        )

    private fun toCeilingDomain(entity: MachineCeilingRuleEntity): CeilingRule =
        CeilingRule(
            ruleKey = entity.ruleKey,
            displayName = entity.displayName,
            ceilingType = entity.ceilingType.toCeilingType(),
            limitValue = entity.limitValue,
            unit = entity.unit,
            resetOnHit = entity.resetOnHit,
            requiresResetFlag = entity.requiresResetFlag,
            description = entity.description,
            isEnabled = entity.isEnabled
        )
}
