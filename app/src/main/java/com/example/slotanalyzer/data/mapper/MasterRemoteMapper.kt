package com.example.slotanalyzer.data.mapper

import com.example.slotanalyzer.data.database.entity.MachineCeilingRuleEntity
import com.example.slotanalyzer.data.database.entity.MachineCounterDefinitionEntity
import com.example.slotanalyzer.data.database.entity.MachineEntity
import com.example.slotanalyzer.data.database.entity.MachineSettingReferenceValueEntity
import com.example.slotanalyzer.data.remote.dto.MachineDetailDto
import javax.inject.Inject

class MasterRemoteMapper @Inject constructor() {

    fun toMachineEntity(detail: MachineDetailDto, now: Long): MachineEntity {
        return MachineEntity(
            id = detail.id,
            name = detail.name,
            manufacturer = detail.manufacturer,
            type = detail.type,
            releaseDate = detail.releaseDate,
            isActive = detail.isActive,
            notes = buildNotes(detail),
            createdAt = now,
            updatedAt = now
        )
    }

    fun toCounterEntities(detail: MachineDetailDto): List<MachineCounterDefinitionEntity> {
        return detail.counterDefinitions.map {
            MachineCounterDefinitionEntity(
                machineId = detail.id,
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
    }

    fun toReferenceEntities(detail: MachineDetailDto): List<MachineSettingReferenceValueEntity> {
        return detail.settingReferenceValues.flatMap { group ->
            group.values.map { value ->
                MachineSettingReferenceValueEntity(
                    machineId = detail.id,
                    counterKey = group.counterKey,
                    settingNo = value.settingNo,
                    denominatorValue = value.denominatorValue,
                    numeratorValue = value.numeratorValue,
                    weight = value.weight,
                    minSampleSize = value.minSampleSize,
                    note = value.note
                )
            }
        }
    }

    fun toCeilingEntities(detail: MachineDetailDto): List<MachineCeilingRuleEntity> {
        return detail.ceilingRules.map {
            MachineCeilingRuleEntity(
                machineId = detail.id,
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

    private fun buildNotes(detail: MachineDetailDto): String? {
        val parts = buildList {
            detail.notes?.takeIf { it.isNotBlank() }?.let { add(it) }
            if (detail.status.isNotBlank()) add("status=${detail.status}")
            if (detail.externalLinks.isNotEmpty()) {
                add("links=${detail.externalLinks.joinToString(",")}")
            }
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }
}