package com.example.slotanalyzer.core.util

import java.util.Locale

object RateFormatter {
    fun calculateRateText(totalGames: Int, count: Int): String {
        if (count <= 0 || totalGames <= 0) return "--"
        return "1/%s".format(
            Locale.US,
            String.format(Locale.US, "%.1f", totalGames.toDouble() / count.toDouble())
        )
    }
}
