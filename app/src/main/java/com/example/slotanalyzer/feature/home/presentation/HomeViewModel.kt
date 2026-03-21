package com.example.slotanalyzer.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.machine.domain.usecase.ObserveMachinesUseCase
import com.example.slotanalyzer.feature.home.domain.usecase.SeedMachineMasterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val machines: List<String> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeMachinesUseCase: ObserveMachinesUseCase,
    private val seedMachineMasterUseCase: SeedMachineMasterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            seedMachineMasterUseCase()
        }
        viewModelScope.launch {
            observeMachinesUseCase().collect { machines ->
                _uiState.value = HomeUiState(
                    machines = machines.map { it.name },
                    isLoading = false
                )
            }
        }
    }
}
