package com.example.slotanalyzer.core.navigation

object AppRoutes {
    const val HOME = "home"
    const val MACHINE_SELECT = "machine_select"
    const val SESSION_INPUT = "session_input/{sessionId}"
    const val INFERENCE = "inference/{sessionId}"
    const val HISTORY_LIST = "history_list"
    const val HISTORY_DETAIL = "history_detail/{sessionId}"

    fun sessionInput(sessionId: String): String = "session_input/$sessionId"
    fun inference(sessionId: String): String = "inference/$sessionId"
    fun historyDetail(sessionId: String): String = "history_detail/$sessionId"
}
