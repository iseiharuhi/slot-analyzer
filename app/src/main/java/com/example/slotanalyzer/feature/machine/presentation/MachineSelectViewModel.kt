package com.example.slotanalyzer.feature.machine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.machine.domain.usecase.ObserveMachinesUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.StartPlaySessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MachineItemUiModel(
    val id: String,
    val name: String
)

data class MachineSelectUiState(
    val machines: List<MachineItemUiModel> = emptyList()
)

@HiltViewModel
class MachineSelectViewModel @Inject constructor(
    private val observeMachinesUseCase: ObserveMachinesUseCase,
    private val startPlaySessionUseCase: StartPlaySessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MachineSelectUiState())
    val uiState: StateFlow<MachineSelectUiState> = _uiState.asStateFlow()

    private val _navigateToSession = MutableSharedFlow<String>()
    val navigateToSession: SharedFlow<String> = _navigateToSession.asSharedFlow()

    init {
        viewModelScope.launch {
            observeMachinesUseCase().collect { machines ->
                _uiState.value = MachineSelectUiState(
                    machines = machines.map {
                        MachineItemUiModel(
                            id = it.id,
                            name = it.name
                        )
                    }
                )
            }
        }
    }

    fun onMachineSelected(machine: MachineItemUiModel) {
        viewModelScope.launch {
            runCatching {
                startPlaySessionUseCase(machine.id)
            }.onSuccess {
                _navigateToSession.emit(machine.id)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}