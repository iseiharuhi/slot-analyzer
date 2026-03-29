package com.example.slotanalyzer.data.remote.api

import com.example.slotanalyzer.data.remote.dto.DiffDto
import com.example.slotanalyzer.data.remote.dto.MachineDetailDto
import com.example.slotanalyzer.data.remote.dto.MachinesIndexDto
import com.example.slotanalyzer.data.remote.dto.VersionDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MasterApi {

    @GET("master/version.json")
    suspend fun getVersion(): VersionDto

    @GET("master/machines.json")
    suspend fun getMachines(): MachinesIndexDto

    @GET("master/diffs/{fromVersion}.json")
    suspend fun getDiff(@Path("fromVersion") fromVersion: Int): DiffDto

    @GET("master/machine_details/{machineId}.json")
    suspend fun getMachineDetail(@Path("machineId") machineId: String): MachineDetailDto
}
