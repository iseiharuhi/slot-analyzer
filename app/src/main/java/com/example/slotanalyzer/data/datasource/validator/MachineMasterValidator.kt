package com.example.slotanalyzer.data.datasource.validator

import com.example.slotanalyzer.data.datasource.model.MachineMasterJson
import javax.inject.Inject

class MachineMasterValidator @Inject constructor() {

    fun validate(master: MachineMasterJson): ValidationResult {
        val errors = mutableListOf<String>()

        if (master.machine.id.isBlank()) errors += "machine.id is required"
        if (master.machine.name.isBlank()) errors += "machine.name is required"

        val keys = master.counterDefinitions.map { it.key }
        if (keys.size != keys.toSet().size) errors += "counter key duplicated"

        master.settingReferenceValues.forEach { group ->
            val settingNos = group.values.map { it.settingNo }
            if (settingNos.size != settingNos.toSet().size) errors += "settingNo duplicated for ${group.counterKey}"
            if (group.counterKey !in keys) errors += "unknown counterKey: ${group.counterKey}"
        }

        return ValidationResult(
            success = errors.isEmpty(),
            errors = errors
        )
    }
}

data class ValidationResult(
    val success: Boolean,
    val errors: List<String>
)
