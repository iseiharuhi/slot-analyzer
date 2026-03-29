package com.example.slotanalyzer.domain.repository

import com.example.slotanalyzer.data.remote.dto.*

interface MasterRepository {

    suspend fun fetchVersion(): VersionDto

    suspend fun fetchDiff(fromVersion: Int): DiffDto

    suspend fun fetchMachineDetail(machineId: String): MachineDetailDto

    suspend fun applyDiff(
        added: List<MachineDetailDto>,
        updated: List<MachineDetailDto>,
        deleted: List<String>
    )

    suspend fun replaceAll(data: List<MachineDetailDto>)
}
