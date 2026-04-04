package com.example.slotanalyzer.domain.model

object MachineFilterKeys {
    const val AT_SMART = "AT_SMART"
    const val NORMAL = "NORMAL"
    const val OKINAWA = "OKINAWA"

    val defaultSelected: Set<String> = setOf(AT_SMART, NORMAL, OKINAWA)
}
