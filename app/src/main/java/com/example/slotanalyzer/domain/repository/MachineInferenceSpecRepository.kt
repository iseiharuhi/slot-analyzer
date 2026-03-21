package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.feature.machine.domain.model.MachineInferenceSpec

interface MachineInferenceSpecRepository {
    suspend fun getByMachineId(machineId: String): MachineInferenceSpec?
    suspend fun getDefaultSpec(): MachineInferenceSpec
}