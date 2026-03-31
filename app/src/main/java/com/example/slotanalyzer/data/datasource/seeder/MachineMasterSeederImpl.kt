package com.example.slotanalyzer.data.datasource.seeder

import com.example.slotanalyzer.data.datasource.model.ManifestMachineItem
import com.example.slotanalyzer.data.datasource.parser.MachineMasterParser
import com.example.slotanalyzer.data.datasource.source.MachineMasterSource
import com.example.slotanalyzer.data.datasource.validator.MachineMasterValidator
import java.security.MessageDigest
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
        val targets = buildSeedTargets(manifest.machines)
        val sourceSignature = buildSourceSignature(manifest.masterVersion, targets)
        val currentVersion = versionStore.getMasterVersion()
        if (currentVersion == sourceSignature) {
            return SeedResult(
                success = true,
                seededMachineCount = 0,
                skippedMachineCount = targets.size,
                masterVersion = manifest.masterVersion,
                errors = emptyList()
            )
        }
        return seed(manifest.masterVersion, sourceSignature, targets)
    }

    override suspend fun forceReseed(): SeedResult {
        val manifest = parser.parseManifest(source.loadManifest())
        val targets = buildSeedTargets(manifest.machines)
        val sourceSignature = buildSourceSignature(manifest.masterVersion, targets)
        return seed(manifest.masterVersion, sourceSignature, targets)
    }

    private suspend fun seed(
        masterVersion: String,
        sourceSignature: String,
        targets: List<ManifestMachineItem>
    ): SeedResult {
        val now = System.currentTimeMillis()
        val errors = mutableListOf<SeedError>()
        var seededCount = 0

        for (item in targets) {
            try {
                val raw = source.loadMachineJson(item.fileName)
                val master = parser.parseMachine(raw)

                if (master.machine.id != item.machineId) {
                    errors += SeedError(
                        machineId = item.machineId,
                        stage = SeedStage.VALIDATE,
                        message = "manifest machineId mismatch: parsed=${master.machine.id}"
                    )
                    continue
                }

                val validation = validator.validate(master)
                if (!validation.success) {
                    validation.errors.forEach {
                        errors += SeedError(item.machineId, SeedStage.VALIDATE, it)
                    }
                    continue
                }

                importer.importMachine(master, now)
                seededCount++
            } catch (e: Exception) {
                errors += SeedError(
                    machineId = item.machineId,
                    stage = SeedStage.IMPORT,
                    message = e.message ?: "unknown error"
                )
            }
        }

        if (errors.isEmpty()) {
            versionStore.saveMasterVersion(sourceSignature, now)
        }

        return SeedResult(
            success = errors.isEmpty(),
            seededMachineCount = seededCount,
            skippedMachineCount = if (errors.isEmpty()) 0 else errors.size,
            masterVersion = masterVersion,
            errors = errors
        )
    }

    private suspend fun buildSeedTargets(manifestItems: List<ManifestMachineItem>): List<ManifestMachineItem> {
        val targetsByMachineId = linkedMapOf<String, ManifestMachineItem>()
        val manifestFileNames = manifestItems.map { it.fileName }.toHashSet()

        manifestItems.forEach { item ->
            targetsByMachineId[item.machineId] = item
        }

        for (fileName in source.listMachineJsonFileNames()) {
            if (fileName.endsWith(".bak", ignoreCase = true)) continue
            if (fileName in manifestFileNames) continue

            val parsed = runCatching {
                parser.parseMachine(source.loadMachineJson(fileName))
            }.getOrNull() ?: continue

            val machineId = parsed.machine.id
            if (machineId.isBlank()) continue
            if (machineId in targetsByMachineId) continue

            targetsByMachineId[machineId] = ManifestMachineItem(
                machineId = machineId,
                fileName = fileName,
                checksum = "auto-discovered"
            )
        }

        return targetsByMachineId.values.toList()
    }

    private fun buildSourceSignature(
        masterVersion: String,
        targets: List<ManifestMachineItem>
    ): String {
        val payload = buildString {
            append(masterVersion)
            append('|')
            targets.sortedBy { it.machineId }.forEach { item ->
                append(item.machineId)
                append(':')
                append(item.fileName)
                append(';')
            }
        }

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { "%02x".format(it) }

        return "$masterVersion:$digest"
    }
}
