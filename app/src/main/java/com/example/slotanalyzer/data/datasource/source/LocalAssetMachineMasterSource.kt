package com.example.slotanalyzer.data.datasource.source

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalAssetMachineMasterSource @Inject constructor(
    @ApplicationContext private val context: Context
) : MachineMasterSource {

    override suspend fun loadManifest(): String = withContext(Dispatchers.IO) {
        context.assets.open("machines/machine_master_manifest.json")
            .bufferedReader()
            .use { it.readText() }
    }

    override suspend fun loadMachineJson(fileName: String): String = withContext(Dispatchers.IO) {
        context.assets.open("machines/$fileName")
            .bufferedReader()
            .use { it.readText() }
    }
}
