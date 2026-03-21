package com.example.slotanalyzer.core.navigation

object AppRoutes {
    const val HOME = "home"
    const val MACHINE_SELECT = "machine_select"
    const val SESSION_INPUT = "session_input/{machineId}"
    const val INFERENCE = "inference"
    const val HISTORY_LIST = "history_list"
    const val HISTORY_DETAIL = "history_detail/{historyId}"

    fun sessionInput(machineId: String): String = "session_input/$machineId"
    fun historyDetail(historyId: String): String = "history_detail/$historyId"
}