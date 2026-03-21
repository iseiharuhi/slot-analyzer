package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.feature.machine.domain.model.Machine
import kotlinx.coroutines.flow.Flow

interface MachineRepository {
    fun observeActiveMachines(): Flow<List<Machine>>
    suspend fun getMachine(machineId: String): Machine?
    suspend fun seedIfNeeded()
}
