package com.example.slotanalyzer.feature.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.feature.machine.domain.usecase.GetMachineUseCase
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import com.example.slotanalyzer.feature.session.domain.usecase.GetCurrentSessionUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.UpdateCounterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(
    val sessionId: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val totalGames: Int = 0,
    val bigCount: Int = 0,
    val regCount: Int = 0,
    val czCount: Int = 0,
    val atCount: Int = 0
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getCurrentSessionUseCase: GetCurrentSessionUseCase,
    private val getMachineUseCase: GetMachineUseCase,
    private val updateCounterUseCase: UpdateCounterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSession()
    }

    private fun loadCurrentSession() {
        viewModelScope.launch {
            val session = getCurrentSessionUseCase()
            val machine = getMachineUseCase(session.machineId)

            _uiState.value = SessionUiState(
                sessionId = session.id,
                machineId = session.machineId,
                machineName = machine?.name.orEmpty(),
                totalGames = session.counters.firstOrNull { it.counterKey == "total_games" }?.intValue ?: 0,
                bigCount = session.counters.firstOrNull { it.counterKey == "big_count" }?.intValue ?: 0,
                regCount = session.counters.firstOrNull { it.counterKey == "reg_count" }?.intValue ?: 0,
                czCount = session.counters.firstOrNull { it.counterKey == "cz_count" }?.intValue ?: 0,
                atCount = session.counters.firstOrNull { it.counterKey == "at_count" }?.intValue ?: 0
            )
        }
    }

    fun increaseTotalGames() = updateTotalGames(_uiState.value.totalGames + 100)
    fun decreaseTotalGames() = updateTotalGames((_uiState.value.totalGames - 100).coerceAtLeast(0))
    fun increaseBigCount() = updateBig(_uiState.value.bigCount + 1)
    fun decreaseBigCount() = updateBig((_uiState.value.bigCount - 1).coerceAtLeast(0))
    fun increaseRegCount() = updateReg(_uiState.value.regCount + 1)
    fun decreaseRegCount() = updateReg((_uiState.value.regCount - 1).coerceAtLeast(0))
    fun increaseCzCount() = updateCz(_uiState.value.czCount + 1)
    fun decreaseCzCount() = updateCz((_uiState.value.czCount - 1).coerceAtLeast(0))
    fun increaseAtCount() = updateAt(_uiState.value.atCount + 1)
    fun decreaseAtCount() = updateAt((_uiState.value.atCount - 1).coerceAtLeast(0))

    fun setTotalGames(value: Int) = updateTotalGames(value.coerceAtLeast(0))
    fun setBigCount(value: Int) = updateBig(value.coerceAtLeast(0))
    fun setRegCount(value: Int) = updateReg(value.coerceAtLeast(0))
    fun setCzCount(value: Int) = updateCz(value.coerceAtLeast(0))
    fun setAtCount(value: Int) = updateAt(value.coerceAtLeast(0))

    private fun updateTotalGames(value: Int) {
        _uiState.update { it.copy(totalGames = value) }
        persist("total_games", value)
    }

    private fun updateBig(value: Int) {
        _uiState.update { it.copy(bigCount = value) }
        persist("big_count", value)
    }

    private fun updateReg(value: Int) {
        _uiState.update { it.copy(regCount = value) }
        persist("reg_count", value)
    }

    private fun updateCz(value: Int) {
        _uiState.update { it.copy(czCount = value) }
        persist("cz_count", value)
    }

    private fun updateAt(value: Int) {
        _uiState.update { it.copy(atCount = value) }
        persist("at_count", value)
    }

    private fun persist(key: String, value: Int) {
        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId
            if (sessionId.isNotBlank()) {
                updateCounterUseCase(
                    UpdateCounterCommand(
                        sessionId = sessionId,
                        counterKey = key,
                        intValue = value,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}