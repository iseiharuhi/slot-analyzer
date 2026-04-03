package com.example.slotanalyzer.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.UserPreferences
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingRepository: AppSettingRepository
) : ViewModel() {

    val uiState: StateFlow<UserPreferences> = appSettingRepository.observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            appSettingRepository.setThemeMode(themeMode)
        }
    }

    fun setShowCeiling(show: Boolean) {
        viewModelScope.launch {
            appSettingRepository.setShowCeiling(show)
        }
    }

    fun setShowExternalLinks(show: Boolean) {
        viewModelScope.launch {
            appSettingRepository.setShowExternalLinks(show)
        }
    }

    fun setMachineSortOrder(sortOrder: String) {
        viewModelScope.launch {
            appSettingRepository.setMachineSortOrder(sortOrder)
        }
    }

    fun setMachineFilter(filter: String) {
        viewModelScope.launch {
            appSettingRepository.setMachineFilter(filter)
        }
    }
}
