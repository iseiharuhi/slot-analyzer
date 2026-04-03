package com.example.slotanalyzer

import androidx.lifecycle.ViewModel
import com.example.slotanalyzer.domain.model.UserPreferences
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

@HiltViewModel
class MainViewModel @Inject constructor(
    appSettingRepository: AppSettingRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = appSettingRepository.observeUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )
}
