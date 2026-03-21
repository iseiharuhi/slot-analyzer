package com.example.slotanalyzer.data.datasource.seeder

import com.example.slotanalyzer.data.datasource.parser.MachineMasterParser
import com.example.slotanalyzer.data.datasource.source.MachineMasterSource
import com.example.slotanalyzer.data.datasource.validator.MachineMasterValidator
import javax.inject.Inject

class MachineMasterSeederImpl @Inject constructor(
    private val source: MachineMasterSource,
    private val parser: MachineMasterParser,
    private val validator: MachineMasterValidator,
    private val importer: MachineMasterImporter,
    private val versionStore: MachineMasterVersionStore
) : MachineMasterSeeder {

    override suspend fun seedIfNeeded(): SeedResult {
        val manifest = parser.parseManifest(source.loadManifest())
        val currentVersion = versionStore.getMasterVersion()
        if (currentVersion == manifest.masterVersion) {
            return SeedResult(
                success = true,
                seededMachineCount = 0,
                skippedMachineCount = manifest.machines.size,
                masterVersion = manifest.masterVersion,
                errors = emptyList()
            )
        }
        return seed(manifest.masterVersion)
    }

    override suspend fun forceReseed(): SeedResult {
        val manifest = parser.parseManifest(source.loadManifest())
        return seed(manifest.masterVersion)
    }

    private suspend fun seed(masterVersion: String): SeedResult {
        val manifest = parser.parseManifest(source.loadManifest())
        val now = System.currentTimeMillis()
        val errors = mutableListOf<SeedError>()
        var seededCount = 0

        for (item in manifest.machines) {
            try {
                val raw = source.loadMachineJson(item.fileName)
                val master = parser.parseMachine(raw)
                val validation = validator.validate(master)
                if (!validation.success) {
                    validation.errors.forEach {
                        errors += SeedError(item.machineId, SeedStage.VALIDATE, it)
                    }
                    return SeedResult(false, seededCount, 0, masterVersion, errors)
                }
                importer.importMachine(master, now)
                seededCount++
            } catch (e: Exception) {
                errors += SeedError(
                    machineId = item.machineId,
                    stage = SeedStage.IMPORT,
                    message = e.message ?: "unknown error"
                )
                return SeedResult(false, seededCount, 0, masterVersion, errors)
            }
        }

        versionStore.saveMasterVersion(masterVersion, now)

        return SeedResult(
            success = true,
            seededMachineCount = seededCount,
            skippedMachineCount = 0,
            masterVersion = masterVersion,
            errors = errors
        )
    }
}
