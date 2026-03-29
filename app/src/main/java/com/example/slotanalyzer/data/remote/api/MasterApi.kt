package com.example.slotanalyzer.data.remote.api

import com.example.slotanalyzer.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path

interface MasterApi {

    @GET("master/version.json")
    suspend fun getVersion(): VersionDto

    @GET("master/diffs/{version}.json")
    suspend fun getDiff(@Path("version") version: Int): DiffDto

    @GET("master/machine_details/{id}.json")
    suspend fun getMachineDetail(@Path("id") id: String): MachineDetailDto
}
