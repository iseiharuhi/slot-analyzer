package com.example.slotanalyzer.feature.machine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.MachineFilter
import com.example.slotanalyzer.domain.model.MachineSortOrder
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.usecase.ObserveMachinesUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.StartPlaySessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MachineItemUiModel(
    val id: String,
    val name: String
)

data class MachineSelectUiState(
    val machines: List<MachineItemUiModel> = emptyList(),
    val sortOrderLabel: String = "リリース日順",
    val filterLabel: String = "すべて"
)

@HiltViewModel
class MachineSelectViewModel @Inject constructor(
    private val observeMachinesUseCase: ObserveMachinesUseCase,
    private val startPlaySessionUseCase: StartPlaySessionUseCase,
    private val appSettingRepository: AppSettingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MachineSelectUiState())
    val uiState: StateFlow<MachineSelectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeMachinesUseCase(),
                appSettingRepository.observeUserPreferences()
            ) { machines, preferences ->
                val filtered = machines
                    .filter { machine -> matchesFilter(machine, preferences.machineFilter) }
                    .sortedWith(machineComparator(preferences.machineSortOrder))

                MachineSelectUiState(
                    machines = filtered.map {
                        MachineItemUiModel(
                            id = it.id,
                            name = it.name
                        )
                    },
                    sortOrderLabel = when (preferences.machineSortOrder) {
                        MachineSortOrder.NAME -> "名前順"
                        else -> "リリース日順"
                    },
                    filterLabel = when (preferences.machineFilter) {
                        MachineFilter.AT_SMART -> "AT / スマスロ"
                        MachineFilter.NORMAL -> "ノーマル"
                        MachineFilter.OKINAWA -> "沖スロ / ハナハナ"
                        else -> "すべて"
                    }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onMachineSelected(machine: MachineItemUiModel) {
        // no-op
    }

    fun startNewSession(machine: MachineItemUiModel, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                startPlaySessionUseCase.startNewSession(machine.id)
            }.onSuccess(onSuccess)
                .onFailure { it.printStackTrace() }
        }
    }

    fun resumeSession(machine: MachineItemUiModel, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                startPlaySessionUseCase(machine.id)
            }.onSuccess(onSuccess)
                .onFailure { it.printStackTrace() }
        }
    }

    private fun machineComparator(sortOrder: String): Comparator<Machine> {
        return when (sortOrder) {
            MachineSortOrder.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            else -> compareByDescending<Machine> { it.releaseDate.orEmpty() }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        }
    }

    private fun matchesFilter(machine: Machine, filter: String): Boolean {
        return when (filter) {
            MachineFilter.AT_SMART -> isAtSmartMachine(machine)
            MachineFilter.NORMAL -> isNormalMachine(machine)
            MachineFilter.OKINAWA -> isOkinawaMachine(machine)
            else -> true
        }
    }

    private fun isAtSmartMachine(machine: Machine): Boolean {
        val type = machine.type.orEmpty().uppercase()
        val name = machine.name
        return type in setOf("AT", "ART", "HYBRID") ||
            name.startsWith("L") ||
            name.contains("スマスロ") ||
            name.contains("BT")
    }

    private fun isNormalMachine(machine: Machine): Boolean {
        return machine.type.orEmpty().uppercase() == "BONUS"
    }

    private fun isOkinawaMachine(machine: Machine): Boolean {
        val name = machine.name
        return listOf(
            "沖ドキ",
            "沖スロ",
            "ハナハナ",
            "チバリヨ",
            "シオサイ",
            "ちゅら",
            "スイカバージョン"
        ).any { keyword -> name.contains(keyword) }
    }
}
