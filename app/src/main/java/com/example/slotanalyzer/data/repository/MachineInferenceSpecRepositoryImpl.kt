package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.feature.machine.domain.model.MachineInferenceSpec
import com.example.slotanalyzer.domain.repository.MachineInferenceSpecRepository
import javax.inject.Inject

class MachineInferenceSpecRepositoryImpl @Inject constructor() : MachineInferenceSpecRepository {

    private val specs = listOf(
        MachineInferenceSpec(
            machineId = "default",
            machineName = "デフォルト機種",
            bigProbabilities = listOf(1.0 / 280.0, 1.0 / 275.0, 1.0 / 268.0, 1.0 / 255.0, 1.0 / 242.0, 1.0 / 228.0),
            regProbabilities = listOf(1.0 / 420.0, 1.0 / 390.0, 1.0 / 360.0, 1.0 / 320.0, 1.0 / 290.0, 1.0 / 260.0),
            czProbabilities = listOf(1.0 / 220.0, 1.0 / 215.0, 1.0 / 210.0, 1.0 / 200.0, 1.0 / 190.0, 1.0 / 180.0),
            atProbabilities = listOf(1.0 / 520.0, 1.0 / 500.0, 1.0 / 470.0, 1.0 / 430.0, 1.0 / 390.0, 1.0 / 350.0)
        ),

        MachineInferenceSpec(
            machineId = "hokuto",
            machineName = "スマスロ北斗の拳",
            bigProbabilities = listOf(1.0 / 273.1, 1.0 / 270.8, 1.0 / 266.4, 1.0 / 254.6, 1.0 / 244.4, 1.0 / 235.0),
            regProbabilities = listOf(1.0 / 439.8, 1.0 / 421.6, 1.0 / 399.6, 1.0 / 360.1, 1.0 / 330.1, 1.0 / 300.2),
            czProbabilities = listOf(1.0 / 240.0, 1.0 / 230.0, 1.0 / 220.0, 1.0 / 210.0, 1.0 / 200.0, 1.0 / 190.0),
            atProbabilities = listOf(1.0 / 520.0, 1.0 / 490.0, 1.0 / 460.0, 1.0 / 420.0, 1.0 / 380.0, 1.0 / 340.0)
        ),

        MachineInferenceSpec(
            machineId = "monster_hunter",
            machineName = "モンスターハンター",
            bigProbabilities = listOf(1.0 / 295.0, 1.0 / 288.0, 1.0 / 276.0, 1.0 / 260.0, 1.0 / 245.0, 1.0 / 230.0),
            regProbabilities = listOf(1.0 / 450.0, 1.0 / 420.0, 1.0 / 390.0, 1.0 / 350.0, 1.0 / 315.0, 1.0 / 280.0),
            czProbabilities = listOf(1.0 / 210.0, 1.0 / 205.0, 1.0 / 198.0, 1.0 / 190.0, 1.0 / 182.0, 1.0 / 175.0),
            atProbabilities = listOf(1.0 / 540.0, 1.0 / 510.0, 1.0 / 480.0, 1.0 / 440.0, 1.0 / 400.0, 1.0 / 360.0)
        )
    )

    override suspend fun getByMachineId(machineId: String): MachineInferenceSpec? {
        return specs.firstOrNull { it.machineId == machineId }
    }

    override suspend fun getDefaultSpec(): MachineInferenceSpec {
        return specs.first { it.machineId == "default" }
    }
}