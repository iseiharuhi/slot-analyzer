package com.example.slotanalyzer.feature.machine.domain.model

data class MachineInferenceSpec(
    val machineId: String,
    val machineName: String,
    val bigProbabilities: List<Double>, // 設定1〜6
    val regProbabilities: List<Double>, // 設定1〜6
    val czProbabilities: List<Double>,  // 設定1〜6
    val atProbabilities: List<Double>   // 設定1〜6
)