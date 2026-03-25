package com.example.slotanalyzer.feature.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.core.ui.model.CeilingStatusUiModel
import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.domain.usecase.CalculateCeilingStatusUseCase
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

data class CeilingInputItemUiModel(
    val ruleKey: String,
    val displayName: String,
    val unit: String,
    val currentValue: Int,
    val note: String? = null
)

data class CeilingBlockUiModel(
    val ruleKey: String,
    val title: String,
    val unit: String,
    val currentValue: Int,
    val limitText: String,
    val remainText: String,
    val summaryText: String,
    val note: String? = null,
    val benefitText: String? = null,
    val resetText: String? = null,
    val steps: List<Int> = listOf(1, 10, 100),
    val isHighlighted: Boolean = false,
    val showInput: Boolean = true
)

data class SessionUiState(
    val sessionId: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val machineTypeText: String = "",
    val guidanceText: String = "",
    val ceilingInputItems: List<CeilingInputItemUiModel> = emptyList(),
    val ceilingItems: List<CeilingStatusUiModel> = emptyList(),
    val ceilingBlocks: List<CeilingBlockUiModel> = emptyList(),
    val counterItems: List<SessionCounterItemUiModel> = emptyList()
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val getMachineUseCase: GetMachineUseCase,
    private val updateCounterUseCase: UpdateCounterUseCase,
    private val resetCurrentSessionCountersUseCase: ResetCurrentSessionCountersUseCase,
    private val calculateCeilingStatusUseCase: CalculateCeilingStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var currentMachine: Machine? = null

    fun loadSession(sessionId: String) {
        if (sessionId.isBlank() || _uiState.value.sessionId == sessionId) return

        viewModelScope.launch {
            val session = getSessionUseCase(sessionId)
            val machine = getMachineUseCase(session.machineId)
            currentMachine = machine

            _uiState.value = buildUiState(session, machine)
        }
    }

    fun setCounter(counterKey: String, value: Int) {
        updateCounter(counterKey, value.coerceAtLeast(0))
    }

    fun setCeilingInput(ruleKey: String, value: Int) {
        val normalizedValue = value.coerceAtLeast(0)
        _uiState.update { currentState ->
            val updatedInputs = currentState.ceilingInputItems.map { item ->
                if (item.ruleKey == ruleKey) item.copy(currentValue = normalizedValue) else item
            }
            val updatedCeilingItems = buildCeilingItems(currentMachine, updatedInputs)
            currentState.copy(
                ceilingInputItems = updatedInputs,
                ceilingItems = updatedCeilingItems,
                ceilingBlocks = buildCeilingBlocks(currentMachine, updatedInputs, updatedCeilingItems)
            )
        }
        persist(ceilingInputStorageKey(ruleKey), normalizedValue)
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
        currentMachine = machine
        _uiState.value = buildUiState(session, machine)
    }

    private fun buildUiState(session: PlaySession, machine: Machine?): SessionUiState {
        val ceilingInputItems = buildCeilingInputItems(session, machine)
        val ceilingItems = buildCeilingItems(machine, ceilingInputItems)

        return SessionUiState(
            sessionId = session.id,
            machineId = session.machineId,
            machineName = machine?.name.orEmpty(),
            machineTypeText = machine?.type.orEmpty(),
            guidanceText = buildGuidanceText(machine),
            ceilingInputItems = ceilingInputItems,
            ceilingItems = ceilingItems,
            ceilingBlocks = buildCeilingBlocks(machine, ceilingInputItems, ceilingItems),
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

    private fun buildCeilingInputItems(
        session: PlaySession,
        machine: Machine?
    ): List<CeilingInputItemUiModel> {
        if (machine == null) return emptyList()

        return machine.ceilingRules
            .filter { it.isEnabled }
            .sortedWith(compareBy({ it.displayOrder }, { it.limitValue }, { it.displayName }))
            .map { rule ->
                CeilingInputItemUiModel(
                    ruleKey = rule.ruleKey,
                    displayName = rule.displayName,
                    unit = rule.unit,
                    currentValue = getCounterValue(session, ceilingInputStorageKey(rule.ruleKey)),
                    note = rule.description
                )
            }
    }

    private fun buildCeilingItems(
        machine: Machine?,
        inputItems: List<CeilingInputItemUiModel>
    ): List<CeilingStatusUiModel> {
        val currentInputs = inputItems.associate { it.ruleKey to it.currentValue }
        return calculateCeilingStatusUseCase(
            machine = machine,
            currentInputs = currentInputs
        ).statuses.map { status ->
            val unitSuffix = status.unit.ifBlank { "G" }
            CeilingStatusUiModel(
                title = status.displayName,
                currentText = "${status.currentValue}$unitSuffix",
                limitText = "${status.limitValue}$unitSuffix",
                remainText = if (status.remainValue <= 0) "到達済み" else "${status.remainValue}$unitSuffix",
                note = status.description,
                isPrimary = false,
                isHighlighted = status.remainValue <= 0
            )
        }
    }

    private fun buildCeilingBlocks(
        machine: Machine?,
        inputItems: List<CeilingInputItemUiModel>,
        statusItems: List<CeilingStatusUiModel>
    ): List<CeilingBlockUiModel> {
        if (machine == null) return emptyList()

        val inputByRuleKey = inputItems.associateBy { it.ruleKey }
        val statusByTitle = statusItems.associateBy { it.title }

        return machine.ceilingRules
            .filter { it.isEnabled }
            .sortedWith(compareBy({ it.displayOrder }, { it.limitValue }, { it.displayName }))
            .map { rule ->
                val input = inputByRuleKey[rule.ruleKey]
                val status = statusByTitle[rule.displayName]
                val unitSuffix = rule.unit.ifBlank { "G" }
                val currentValue = input?.currentValue ?: 0
                val remainText = status?.remainText ?: "ー"
                val currentText = "${currentValue}$unitSuffix"
                val summaryText = if (remainText == "到達済み") {
                    "現在 $currentText / 到達済み"
                } else {
                    "現在 $currentText / 残り $remainText"
                }

                CeilingBlockUiModel(
                    ruleKey = rule.ruleKey,
                    title = rule.displayName,
                    unit = rule.unit,
                    currentValue = currentValue,
                    limitText = status?.limitText ?: "${rule.limitValue}$unitSuffix",
                    remainText = remainText,
                    summaryText = summaryText,
                    note = rule.description,
                    benefitText = rule.benefitText,
                    resetText = rule.resetText,
                    steps = buildCeilingSteps(rule.unit, rule.stepValue),
                    isHighlighted = remainText == "到達済み",
                    showInput = rule.showInput
                )
            }
    }

    private fun buildGuidanceText(machine: Machine?): String {
        if (machine == null) return ""

        return when (machine.type?.uppercase()) {
            "AT" -> "天井条件ごとにカードを開いて、現在値入力と恩恵確認を行えます。"
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

    private fun buildCeilingSteps(unit: String, stepValue: Int): List<Int> {
        val normalizedStep = stepValue.coerceAtLeast(1)
        return when (unit.lowercase()) {
            "g" -> listOf(1, 10, 100)
            "pt" -> listOf(10, 50, 100)
            else -> listOf(1, normalizedStep, normalizedStep * 5).distinct().sorted()
        }
    }

    private fun ceilingInputStorageKey(ruleKey: String): String = "ceiling::$ruleKey"

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
    }
}
