package com.example.slotanalyzer.feature.machine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.machine.domain.usecase.ObserveMachinesUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.StartPlaySessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    /**
     * 機種を選択しただけでは何もしない。
     * 方式Bでは、続きから再開か新規実戦開始かをユーザーに選ばせる。
     */
    fun onMachineSelected(machine: MachineItemUiModel) {
        // no-op
    }

    /**
     * 新規実戦開始ボタン用。
     * 明示的に新しいセッションを開始したい時だけ呼ぶ。
     */
    fun startNewSession(machine: MachineItemUiModel, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                startPlaySessionUseCase.startNewSession(machine.id)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}