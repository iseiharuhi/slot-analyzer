package com.example.slotanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.core.navigation.AppNavHost
import com.example.slotanalyzer.domain.model.ThemeMode
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import com.example.slotanalyzer.ui.theme.SlotSettingAnalyzerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appSettingRepository: AppSettingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences by appSettingRepository.observeUserPreferences()
                .collectAsStateWithLifecycle(
                    initialValue = com.example.slotanalyzer.domain.model.UserPreferences()
                )

            val useDarkTheme = remember(preferences.themeMode) {
                when (preferences.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    else -> null
                }
            }

            SlotSettingAnalyzerTheme(
                darkTheme = useDarkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()
            ) {
                AppNavHost()
            }
        }
    }
}
