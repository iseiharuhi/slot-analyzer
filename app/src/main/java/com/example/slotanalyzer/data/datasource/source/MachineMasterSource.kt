package com.example.slotanalyzer.data.datasource.source

interface MachineMasterSource {
    suspend fun loadManifest(): String
    suspend fun loadMachineJson(fileName: String): String
}
