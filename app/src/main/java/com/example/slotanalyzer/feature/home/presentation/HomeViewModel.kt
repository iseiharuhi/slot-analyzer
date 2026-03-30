package com.example.slotanalyzer.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.home.domain.usecase.SeedMachineMasterUseCase
import com.example.slotanalyzer.feature.home.domain.usecase.SyncMasterDataUseCase
import com.example.slotanalyzer.feature.machine.domain.usecase.ObserveMachinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val machines: List<String> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeMachinesUseCase: ObserveMachinesUseCase,
    private val seedMachineMasterUseCase: SeedMachineMasterUseCase,
    private val syncMasterDataUseCase: SyncMasterDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            seedMachineMasterUseCase()
            // 現行の remote master は assets/machines とURL仕様が一致していないため、
            // リンクが欠落したデータで上書きされるのを防ぐまで起動時同期は止める。
            // syncMasterDataUseCase()
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
