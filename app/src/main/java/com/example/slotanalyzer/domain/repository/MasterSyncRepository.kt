package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.data.remote.dto.*

interface MasterSyncRepository {

    suspend fun fetchVersion(): VersionDto

    suspend fun fetchDiff(fromVersion: Int): DiffDto

    suspend fun fetchMachinesIndex(): MachinesIndexDto

    suspend fun fetchMachineDetail(machineId: String): MachineDetailDto

    suspend fun upsertMachineDetail(detail: MachineDetailDto, now: Long)

    suspend fun deleteMachines(ids: List<String>)

    suspend fun replaceAll(details: List<MachineDetailDto>, now: Long)
}