package com.example.slotanalyzer.data.datasource.parser

import com.example.slotanalyzer.data.datasource.model.MachineManifestJson
import com.example.slotanalyzer.data.datasource.model.MachineMasterJson
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MachineMasterParser @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseManifest(raw: String): MachineManifestJson = json.decodeFromString(raw)

    fun parseMachine(raw: String): MachineMasterJson = json.decodeFromString(raw)
}
