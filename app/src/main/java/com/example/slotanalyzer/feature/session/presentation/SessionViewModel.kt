package com.example.slotanalyzer.feature.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.feature.machine.domain.model.MachineCounterDefinition
import com.example.slotanalyzer.feature.machine.domain.usecase.GetMachineUseCase
import com.example.slotanalyzer.feature.session.domain.model.PlaySession
import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import com.example.slotanalyzer.feature.session.domain.usecase.GetSessionUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.ResetCurrentSessionCountersUseCase
import com.example.slotanalyzer.feature.session.domain.usecase.UpdateCounterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionCounterItemUiModel(
    val key: String,
    val displayName: String,
    val value: Int,
    val unit: String,
    val step: Int,
    val supportsMinus: Boolean,
    val categoryLabel: String,
    val note: String? = null,
    val rateText: String? = null
)

data class SessionUiState(
    val sessionId: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val machineTypeText: String = "",
    val guidanceText: String = "",
    val counterItems: List<SessionCounterItemUiModel> = emptyList()
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val getMachineUseCase: GetMachineUseCase,
    private val updateCounterUseCase: UpdateCounterUseCase,
    private val resetCurrentSessionCountersUseCase: ResetCurrentSessionCountersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: String) {
        if (sessionId.isBlank() || _uiState.value.sessionId == sessionId) return

        viewModelScope.launch {
            val session = getSessionUseCase(sessionId)
            val machine = getMachineUseCase(session.machineId)

            _uiState.value = buildUiState(session, machine)
        }
    }

    fun setCounter(counterKey: String, value: Int) {
        updateCounter(counterKey, value.coerceAtLeast(0))
    }

    fun resetAllCounters() {
        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId
            if (sessionId.isBlank()) return@launch
            resetCurrentSessionCountersUseCase(sessionId)
            loadSessionForce(sessionId)
        }
    }

    private suspend fun loadSessionForce(sessionId: String) {
        val session = getSessionUseCase(sessionId)
        val machine = getMachineUseCase(session.machineId)
        _uiState.value = buildUiState(session, machine)
    }

    private fun buildUiState(session: PlaySession, machine: Machine?): SessionUiState {
        return SessionUiState(
            sessionId = session.id,
            machineId = session.machineId,
            machineName = machine?.name.orEmpty(),
            machineTypeText = machine?.type.orEmpty(),
            guidanceText = buildGuidanceText(machine),
            counterItems = buildCounterItems(machine, session)
        )
    }

    private fun updateCounter(counterKey: String, newValue: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.counterItems.map { item ->
                if (item.key == counterKey) item.copy(value = newValue) else item
            }

            val totalGames = updatedItems.firstOrNull { it.key == TOTAL_GAMES_KEY }?.value ?: 0

            currentState.copy(
                counterItems = updatedItems.map { item ->
                    item.copy(
                        rateText = buildRateText(
                            counterKey = item.key,
                            totalGames = totalGames,
                            count = item.value
                        )
                    )
                }
            )
        }

        persist(counterKey, newValue)
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

    private fun buildCounterItems(
        machine: Machine?,
        session: PlaySession
    ): List<SessionCounterItemUiModel> {
        if (machine == null) return emptyList()

        val totalGames = getCounterValue(session, TOTAL_GAMES_KEY)

        return machine.counters
            .sortedBy { it.sortOrder }
            .filter { shouldShowCounter(machine, it) }
            .map { definition ->
                val value = getCounterValue(session, definition.key)

                SessionCounterItemUiModel(
                    key = definition.key,
                    displayName = definition.displayName,
                    value = value,
                    unit = definition.unit,
                    step = if (definition.key == TOTAL_GAMES_KEY) 100 else 1,
                    supportsMinus = definition.supportsMinus || definition.key == TOTAL_GAMES_KEY,
                    categoryLabel = definition.category.toLabel(),
                    note = definition.notes,
                    rateText = buildRateText(
                        counterKey = definition.key,
                        totalGames = totalGames,
                        count = value
                    )
                )
            }
    }

    private fun shouldShowCounter(
        machine: Machine,
        definition: MachineCounterDefinition
    ): Boolean {
        if (!definition.isEnabled) return false
        if (!definition.isDefaultVisible) return false
        if (definition.key == TOTAL_GAMES_KEY) return true

        val references = machine.settingReferenceValues.filter { it.counterKey == definition.key }
        if (references.isEmpty()) return definition.category != CounterCategory.SPECIAL

        val denominatorValues = references.mapNotNull { it.denominatorValue }
        if (denominatorValues.isEmpty()) return false

        return denominatorValues.distinct().size > 1
    }

    private fun buildGuidanceText(machine: Machine?): String {
        if (machine == null) return ""

        return when (machine.type?.uppercase()) {
            "AT" -> "AT・CZ系の初当たりを中心に入力すると、推測精度が上がります。"
            "A", "A+ART", "A+AT" -> "ボーナス回数を中心に入力して、総ゲーム数とのバランスを見てください。"
            else -> "機種ごとの設定差がある項目だけ表示しています。"
        }
    }

    private fun buildRateText(
        counterKey: String,
        totalGames: Int,
        count: Int
    ): String? {
        if (counterKey == TOTAL_GAMES_KEY) return null
        if (count <= 0 || totalGames <= 0) return null
        return "1/${"%.1f".format(totalGames.toDouble() / count.toDouble())}"
    }

    private fun getCounterValue(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    private fun CounterCategory.toLabel(): String {
        return when (this) {
            CounterCategory.BASIC -> "基本"
            CounterCategory.BONUS -> "ボーナス"
            CounterCategory.AT_CZ -> "AT / CZ"
            CounterCategory.SMALL_ROLE -> "小役"
            CounterCategory.SPECIAL -> "特殊"
        }
    }

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
    }
}
