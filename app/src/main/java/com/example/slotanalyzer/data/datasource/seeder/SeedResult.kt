package com.example.slotanalyzer.data.datasource.seeder

data class SeedResult(
    val success: Boolean,
    val seededMachineCount: Int,
    val skippedMachineCount: Int,
    val masterVersion: String?,
    val errors: List<SeedError>
)

data class SeedError(
    val machineId: String?,
    val stage: SeedStage,
    val message: String
)

enum class SeedStage {
    LOAD_MANIFEST,
    LOAD_MACHINE_JSON,
    PARSE,
    VALIDATE,
    MAP,
    IMPORT,
    VERSION_SAVE
}
