package com.example.slotanalyzer.data.datasource.seeder

import com.example.slotanalyzer.data.database.dao.MachineDao
import com.example.slotanalyzer.data.datasource.parser.MachineMasterParser
import com.example.slotanalyzer.data.datasource.source.MachineMasterSource
import com.example.slotanalyzer.data.datasource.validator.MachineMasterValidator
import javax.inject.Inject

class MachineMasterSeederImpl @Inject constructor(
    private val source: MachineMasterSource,
    private val parser: MachineMasterParser,
    private val validator: MachineMasterValidator,
    private val importer: MachineMasterImporter,
    private val versionStore: MachineMasterVersionStore,
    private val machineDao: MachineDao
) : MachineMasterSeeder {

    override suspend fun seedIfNeeded(): SeedResult {
        return if (machineDao.countMachines() > 0) {
            SeedResult(
                success = true,
                seededMachineCount = 0,
                skippedMachineCount = 0,
                masterVersion = versionStore.getMasterVersion(),
                errors = emptyList()
            )
        } else {
            forceReseed()
        }
    }

    override suspend fun forceReseed(): SeedResult {
        val now = System.currentTimeMillis()
        val errors = mutableListOf<SeedError>()
        val fileNames = try {
            source.listMachineJsonFileNames()
        } catch (e: Exception) {
            return SeedResult(
                success = false,
                seededMachineCount = 0,
                skippedMachineCount = 0,
                masterVersion = null,
                errors = listOf(
                    SeedError(
                        machineId = null,
                        stage = SeedStage.LOAD_MACHINE_JSON,
                        message = e.message ?: "failed to list asset json files"
                    )
                )
            )
        }

        var seeded = 0
        var skipped = 0

        fileNames.forEach { fileName ->
            val raw = try {
                source.loadMachineJson(fileName)
            } catch (e: Exception) {
                errors += SeedError(
                    machineId = null,
                    stage = SeedStage.LOAD_MACHINE_JSON,
                    message = "$fileName: ${e.message ?: "failed to load"}"
                )
                skipped++
                return@forEach
            }

            val master = try {
                parser.parseMachine(raw)
            } catch (e: Exception) {
                errors += SeedError(
                    machineId = null,
                    stage = SeedStage.PARSE,
                    message = "$fileName: ${e.message ?: "failed to parse"}"
                )
                skipped++
                return@forEach
            }

            val validation = validator.validate(master)
            if (!validation.success) {
                errors += SeedError(
                    machineId = master.machine.id.ifBlank { null },
                    stage = SeedStage.VALIDATE,
                    message = "$fileName: ${validation.errors.joinToString()}"
                )
                skipped++
                return@forEach
            }

            try {
                importer.importMachine(master, now)
                seeded++
            } catch (e: Exception) {
                errors += SeedError(
                    machineId = master.machine.id.ifBlank { null },
                    stage = SeedStage.IMPORT,
                    message = "$fileName: ${e.message ?: "failed to import"}"
                )
                skipped++
            }
        }

        val masterVersion = "assets-direct-${fileNames.size}"
        try {
            versionStore.saveMasterVersion(masterVersion, now)
        } catch (e: Exception) {
            errors += SeedError(
                machineId = null,
                stage = SeedStage.VERSION_SAVE,
                message = e.message ?: "failed to save master version"
            )
        }

        return SeedResult(
            success = errors.isEmpty(),
            seededMachineCount = seeded,
            skippedMachineCount = skipped,
            masterVersion = masterVersion,
            errors = errors
        )
    }
}
