package com.example.slotanalyzer.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.MachineFilterKeys
import com.example.slotanalyzer.domain.model.MachineSortOrder
import com.example.slotanalyzer.domain.model.ThemeMode
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = ThemeMode.SYSTEM,
    val keepScreenOn: Boolean = false,
    val showCeiling: Boolean = true,
    val showExternalLinks: Boolean = true,
    val machineSortOrder: String = MachineSortOrder.RELEASE_DATE,
    val selectedMachineFilters: Set<String> = MachineFilterKeys.defaultSelected,
    val hideUpcomingMachines: Boolean = false
) {
    val isAllTypesSelected: Boolean
        get() = selectedMachineFilters.containsAll(MachineFilterKeys.defaultSelected)
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingRepository: AppSettingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingRepository.observeUserPreferences().collect { preferences ->
                _uiState.value = SettingsUiState(
                    themeMode = preferences.themeMode,
                    keepScreenOn = preferences.keepScreenOn,
                    showCeiling = preferences.showCeiling,
                    showExternalLinks = preferences.showExternalLinks,
                    machineSortOrder = preferences.machineSortOrder,
                    selectedMachineFilters = preferences.selectedMachineFilters,
                    hideUpcomingMachines = preferences.hideUpcomingMachines
                )
            }
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            appSettingRepository.setThemeMode(themeMode)
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            appSettingRepository.setKeepScreenOn(enabled)
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

    fun setHideUpcomingMachines(hide: Boolean) {
        viewModelScope.launch {
            appSettingRepository.setHideUpcomingMachines(hide)
        }
    }

    fun setShowAllTypes(showAll: Boolean) {
        viewModelScope.launch {
            appSettingRepository.setSelectedMachineFilters(
                if (showAll) MachineFilterKeys.defaultSelected else emptySet()
            )
        }
    }

    fun toggleMachineFilter(filterKey: String) {
        val current = _uiState.value.selectedMachineFilters.toMutableSet()
        if (current.contains(filterKey)) {
            current.remove(filterKey)
        } else {
            current.add(filterKey)
        }

        viewModelScope.launch {
            appSettingRepository.setSelectedMachineFilters(current)
        }

        _uiState.update { it.copy(selectedMachineFilters = current) }
    }
}
