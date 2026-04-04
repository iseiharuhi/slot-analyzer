package com.example.slotanalyzer.feature.machine.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.MachineFilterKeys
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
    val filterLabel: String = "すべて表示"
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
                    .filter { machine -> matchesFilter(machine, preferences.selectedMachineFilters) }
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
                    filterLabel = buildFilterLabel(preferences.selectedMachineFilters)
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

    private fun matchesFilter(machine: Machine, filters: Set<String>): Boolean {
        if (filters.isEmpty() || filters.containsAll(MachineFilterKeys.defaultSelected)) {
            return true
        }

        return filters.any { filter ->
            when (filter) {
                MachineFilterKeys.AT_SMART -> isAtSmartMachine(machine)
                MachineFilterKeys.NORMAL -> isNormalMachine(machine)
                MachineFilterKeys.OKINAWA -> isOkinawaMachine(machine)
                else -> false
            }
        }
    }

    private fun isAtSmartMachine(machine: Machine): Boolean {
        val type = machine.type.orEmpty().uppercase()
        val name = machine.name
        return type in setOf("AT", "ART", "BT", "A+AT", "A+ART", "ST", "BONUS+AT") ||
            name.startsWith("L") ||
            name.contains("スマスロ") ||
            name.contains("BT")
    }

    private fun isNormalMachine(machine: Machine): Boolean {
        val type = machine.type.orEmpty().uppercase()
        val name = machine.name
        return type in setOf("A", "A+RT", "A+AT", "BONUS") ||
            listOf("ジャグラー", "ハナビ", "バーサス", "サンダーV", "クランキー", "アレックス", "ディスクアップ", "ひぐらし", "ファミスタ")
                .any { keyword -> name.contains(keyword) }
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

    private fun buildFilterLabel(filters: Set<String>): String {
        if (filters.isEmpty() || filters.containsAll(MachineFilterKeys.defaultSelected)) {
            return "すべて表示"
        }

        val labels = buildList {
            if (filters.contains(MachineFilterKeys.AT_SMART)) add("AT / スマスロ")
            if (filters.contains(MachineFilterKeys.NORMAL)) add("ノーマル")
            if (filters.contains(MachineFilterKeys.OKINAWA)) add("沖スロ / ハナハナ")
        }

        return labels.joinToString("・").ifBlank { "すべて表示" }
    }
}
