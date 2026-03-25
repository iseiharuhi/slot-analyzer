package com.example.slotanalyzer.feature.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.CounterCategory
import com.example.slotanalyzer.domain.usecase.CalculateCeilingStatusUseCase
import com.example.slotanalyzer.feature.machine.domain.model.CeilingInputMode
import com.example.slotanalyzer.feature.machine.domain.model.CeilingRule
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

data class CeilingConditionUiModel(
    val conditionId: String,
    val title: String,
    val currentValue: Int?,
    val limitValue: Int,
    val limitText: String,
    val remainText: String,
    val inputMode: CeilingInputMode,
    val stepValue: Int,
    val showInput: Boolean,
    val isPrimary: Boolean,
    val isHighlighted: Boolean,
    val benefitText: String?,
    val resetText: String?
)

data class CeilingSectionUiModel(
    val isSupported: Boolean = false,
    val isExpanded: Boolean = false,
    val items: List<CeilingConditionUiModel> = emptyList(),
    val primaryItems: List<CeilingConditionUiModel> = emptyList(),
    val otherSummaryText: String? = null
)

data class SessionUiState(
    val sessionId: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val machineTypeText: String = "",
    val guidanceText: String = "",
    val ceilingSection: CeilingSectionUiModel = CeilingSectionUiModel(),
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
            _uiState.value = buildUiState(session, machine, expanded = false)
        }
    }

    fun setCounter(counterKey: String, value: Int) {
        updateCounter(counterKey, value.coerceAtLeast(0))
    }

    fun setCeilingValue(conditionId: String, value: Int?) {
        val rule = findRule(conditionId)
        _uiState.update { currentState ->
            val updatedItems = currentState.ceilingSection.items.map { item ->
                if (item.conditionId == conditionId) buildCeilingConditionUiModel(rule, value) ?: item
                else item
            }
            currentState.copy(
                ceilingSection = buildCeilingSection(
                    machine = currentMachine,
                    items = updatedItems,
                    isExpanded = currentState.ceilingSection.isExpanded
                )
            )
        }
        persist(ceilingKey(conditionId), value)
        if (rule?.ceilingType == com.example.slotanalyzer.domain.model.CeilingType.GAME && rule.isPrimary) {
            persist(CURRENT_GAME_KEY, value ?: 0)
        }
    }

    fun toggleCeilingExpanded() {
        _uiState.update { currentState ->
            currentState.copy(
                ceilingSection = currentState.ceilingSection.copy(
                    isExpanded = !currentState.ceilingSection.isExpanded
                )
            )
        }
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
        _uiState.value = buildUiState(session, machine, expanded = _uiState.value.ceilingSection.isExpanded)
    }

    private fun buildUiState(session: PlaySession, machine: Machine?, expanded: Boolean): SessionUiState {
        val ceilingItems = buildCeilingItems(machine, session)
        return SessionUiState(
            sessionId = session.id,
            machineId = session.machineId,
            machineName = machine?.name.orEmpty(),
            machineTypeText = machine?.type.orEmpty(),
            guidanceText = buildGuidanceText(machine),
            ceilingSection = buildCeilingSection(machine, ceilingItems, expanded),
            counterItems = buildCounterItems(machine, session)
        )
    }

    private fun buildCeilingItems(machine: Machine?, session: PlaySession): List<CeilingConditionUiModel> {
        return machine.orEmptyRules()
            .mapNotNull { rule ->
                val value = getOptionalCounterValue(session, ceilingKey(rule.ruleKey))
                buildCeilingConditionUiModel(rule, value)
            }
    }

    private fun buildCeilingConditionUiModel(rule: CeilingRule?, value: Int?): CeilingConditionUiModel? {
        if (rule == null || !rule.isEnabled || rule.ceilingType == com.example.slotanalyzer.domain.model.CeilingType.COMPOSITE) return null
        val unitLabel = if (rule.unit.isBlank()) defaultUnit(rule) else rule.unit
        val remainText = when {
            value == null -> "ー"
            value >= rule.limitValue -> "到達済み"
            else -> "${rule.limitValue - value}$unitLabel"
        }
        return CeilingConditionUiModel(
            conditionId = rule.ruleKey,
            title = rule.displayName,
            currentValue = value,
            limitValue = rule.limitValue,
            limitText = "${rule.limitValue}$unitLabel",
            remainText = remainText,
            inputMode = rule.inputMode,
            stepValue = rule.stepValue.coerceAtLeast(1),
            showInput = rule.showInput,
            isPrimary = rule.isPrimary,
            isHighlighted = rule.isHighlighted,
            benefitText = rule.benefitText ?: rule.description,
            resetText = rule.resetText
        )
    }

    private fun buildCeilingSection(
        machine: Machine?,
        items: List<CeilingConditionUiModel>,
        isExpanded: Boolean
    ): CeilingSectionUiModel {
        val supportedItems = items.sortedWith(compareBy<CeilingConditionUiModel> { findRule(it.conditionId)?.displayOrder ?: Int.MAX_VALUE }.thenBy { it.title })
        if (machine == null || supportedItems.isEmpty()) {
            return CeilingSectionUiModel(isSupported = false, isExpanded = false)
        }
        val primaryItems = supportedItems.filter { it.isPrimary }.ifEmpty { supportedItems.take(1) }
        val others = (supportedItems.size - primaryItems.size).coerceAtLeast(0)
        return CeilingSectionUiModel(
            isSupported = true,
            isExpanded = isExpanded,
            items = supportedItems,
            primaryItems = primaryItems,
            otherSummaryText = if (others > 0) "その他${others}種類あり" else null
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

    private fun persist(key: String, value: Int?) {
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
                    rateText = buildRateText(definition.key, totalGames, value)
                )
            }
    }

    private fun shouldShowCounter(machine: Machine, definition: MachineCounterDefinition): Boolean {
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

    private fun buildRateText(counterKey: String, totalGames: Int, count: Int): String? {
        if (counterKey == TOTAL_GAMES_KEY) return null
        if (count <= 0 || totalGames <= 0) return null
        return "1/${"%.1f".format(totalGames.toDouble() / count.toDouble())}"
    }

    private fun getCounterValue(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    private fun getOptionalCounterValue(session: PlaySession, key: String): Int? {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue
    }

    private fun findRule(conditionId: String): CeilingRule? = currentMachine.orEmptyRules().firstOrNull { it.ruleKey == conditionId }

    private fun Machine?.orEmptyRules(): List<CeilingRule> = this?.ceilingRules.orEmpty().filter { it.isEnabled && it.ceilingType != com.example.slotanalyzer.domain.model.CeilingType.COMPOSITE }

    private fun defaultUnit(rule: CeilingRule): String = when (rule.ceilingType) {
        com.example.slotanalyzer.domain.model.CeilingType.GAME -> "G"
        com.example.slotanalyzer.domain.model.CeilingType.COUNT -> "回"
        com.example.slotanalyzer.domain.model.CeilingType.CYCLE -> "周期"
        com.example.slotanalyzer.domain.model.CeilingType.POINT -> "pt"
        com.example.slotanalyzer.domain.model.CeilingType.COMPOSITE -> ""
    }

    private fun CounterCategory.toLabel(): String = when (this) {
        CounterCategory.BASIC -> "基本"
        CounterCategory.BONUS -> "ボーナス"
        CounterCategory.AT_CZ -> "AT / CZ"
        CounterCategory.SMALL_ROLE -> "小役"
        CounterCategory.SPECIAL -> "特殊"
    }

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
        private const val CURRENT_GAME_KEY = "current_game"
        private const val CEILING_PREFIX = "ceiling::"
        private fun ceilingKey(ruleKey: String): String = "$CEILING_PREFIX$ruleKey"
    }
}
