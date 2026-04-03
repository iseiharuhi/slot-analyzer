package com.example.slotanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.core.navigation.AppNavHost
import com.example.slotanalyzer.domain.model.ThemeMode
import com.example.slotanalyzer.ui.theme.SlotSettingAnalyzerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userPreferences = viewModel.userPreferences.collectAsStateWithLifecycle().value
            val darkTheme = when (userPreferences.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }

            SlotSettingAnalyzerTheme(darkTheme = darkTheme) {
                AppNavHost()
            }
        }
    }
}
