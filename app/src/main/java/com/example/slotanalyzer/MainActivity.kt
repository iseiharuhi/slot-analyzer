package com.example.slotanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.slotanalyzer.core.navigation.AppNavHost
import com.example.slotanalyzer.ui.theme.SlotSettingAnalyzerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlotSettingAnalyzerTheme {
                AppNavHost()
            }
        }
    }
}
