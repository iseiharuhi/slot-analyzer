package com.example.slotanalyzer.data.datasource.source

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalAssetMachineMasterSource @Inject constructor(
    @ApplicationContext private val context: Context
) : MachineMasterSource {

    override suspend fun loadManifest(): String {
        return """
            {
              "schemaVersion": 1,
              "masterVersion": "assets-direct",
              "machines": []
            }
        """.trimIndent()
    }

    override suspend fun loadMachineJson(fileName: String): String {
        return context.assets
            .open("machines/$fileName")
            .bufferedReader()
            .use { it.readText() }
    }

    override suspend fun listMachineJsonFileNames(): List<String> {
        return (context.assets.list("machines") ?: emptyArray())
            .filter { it.endsWith(".json") }
            .filter { it != "machine_master_manifest.json" }
            .sorted()
    }
}
