package com.example.slotanalyzer.feature.machine.domain.usecase

import com.example.slotanalyzer.domain.repository.MachineRepository
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveMachinesUseCase @Inject constructor(
    private val repository: MachineRepository
) {
    operator fun invoke(): Flow<List<Machine>> {
        return repository.observeActiveMachines()
            .map { machines ->
                machines
                    .distinctBy { machine ->
                        normalizeName(machine.name)
                    }
            }
    }

    private fun normalizeName(name: String): String {
        return name
            .replace(" ", "")
            .replace("　", "")
            .replace("〜", "")
            .replace("～", "")
            .replace("ver.", "", ignoreCase = true)
            .replace("version", "", ignoreCase = true)
            .replace("Ver.", "", ignoreCase = true)
            .replace("Ｖ", "V")
            .replace("Ⅰ", "1")
            .replace("Ⅱ", "2")
            .replace("Ⅲ", "3")
            .lowercase()
    }
}