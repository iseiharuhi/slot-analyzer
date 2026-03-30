package com.example.slotanalyzer.feature.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slotanalyzer.domain.model.CeilingType
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
    val value: Int?,
    val unit: String,
    val step: Int,
    val supportsMinus: Boolean,
    val categoryLabel: String,
    val note: String? = null,
    val rateText: String? = null,
    val allowEmpty: Boolean = true
)

data class CeilingBlockUiModel(
    val ruleKey: String,
    val title: String,
    val currentValue: Int,
    val currentText: String,
    val limitText: String,
    val remainText: String,
    val summaryText: String,
    val unit: String,
    val steps: List<Int>,
    val inputVisible: Boolean,
    val note: String? = null,
    val benefitText: String? = null,
    val resetText: String? = null,
    val isPrimary: Boolean = false,
    val isHighlighted: Boolean = false,
    val isComposite: Boolean = false
)

data class SessionUiState(
    val sessionId: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val machineTypeText: String = "",
    val dmmUrl: String? = null,
    val ichigekiUrl: String? = null,
    val guidanceText: String = "",
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

    fun setCounter(counterKey: String, value: Int?) {
        val normalizedValue = value?.coerceAtLeast(0)
        updateCounter(counterKey, normalizedValue)
    }

    fun setCeilingInput(ruleKey: String, value: Int) {
        val normalizedValue = value.coerceAtLeast(0)
        _uiState.update { currentState ->
            currentState.copy(
                ceilingBlocks = currentState.ceilingBlocks.map { block ->
                    if (block.ruleKey == ruleKey) {
                        block.copy(currentValue = normalizedValue)
                    } else {
                        block
                    }
                }.let { updated ->
                    rebuildCeilingBlocks(currentMachine, updated)
                }
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
        val ceilingBlocks = buildCeilingBlocks(session, machine)

        return SessionUiState(
            sessionId = session.id,
            machineId = session.machineId,
            machineName = machine?.name.orEmpty(),
            machineTypeText = machine?.type.orEmpty(),
            dmmUrl = machine?.dmmUrl,
            ichigekiUrl = machine?.ichigekiUrl,
            guidanceText = buildGuidanceText(machine),
            ceilingBlocks = ceilingBlocks,
            counterItems = buildCounterItems(machine, session)
        )
    }

    private fun updateCounter(counterKey: String, newValue: Int?) {
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
                val value = getCounterValueOrNull(session, definition.key)

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
                    ),
                    allowEmpty = true
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

    private fun buildCeilingBlocks(
        session: PlaySession,
        machine: Machine?
    ): List<CeilingBlockUiModel> {
        if (machine == null) return emptyList()

        val sortedRules = machine.ceilingRules
            .filter { it.isEnabled }
            .sortedWith(compareBy<CeilingRule> { it.displayOrder }.thenBy { it.limitValue })

        val currentInputs = sortedRules.associate { rule ->
            rule.ruleKey to getCounterValue(session, ceilingInputStorageKey(rule.ruleKey))
        }

        val statusesByRuleKey = calculateCeilingStatusUseCase(
            machine = machine,
            currentInputs = currentInputs
        ).statuses.associateBy { it.ruleKey }

        return sortedRules.map { rule ->
            val status = statusesByRuleKey[rule.ruleKey]
            val currentValue = status?.currentValue ?: 0
            val limitText = formatValue(status?.limitValue ?: rule.limitValue, status?.unit ?: rule.unit, rule.ceilingType)
            val remainText = when {
                status == null -> "-"
                status.remainValue <= 0 -> "到達済み"
                else -> formatValue(status.remainValue, status.unit, rule.ceilingType)
            }
            val currentText = formatValue(currentValue, status?.unit ?: rule.unit, rule.ceilingType)
            val note = buildSupportNote(rule)
            val summary = if (remainText == "到達済み") {
                "現在 $currentText / 到達済み"
            } else {
                "現在 $currentText / 残り $remainText"
            }

            CeilingBlockUiModel(
                ruleKey = rule.ruleKey,
                title = rule.displayName,
                currentValue = currentValue,
                currentText = currentText,
                limitText = limitText,
                remainText = remainText,
                summaryText = summary,
                unit = status?.unit ?: rule.unit,
                steps = buildCeilingSteps(rule),
                inputVisible = rule.showInput && rule.inputMode != CeilingInputMode.READ_ONLY && rule.ceilingType != CeilingType.COMPOSITE,
                note = note,
                benefitText = rule.benefitText,
                resetText = rule.resetText,
                isPrimary = status?.isPrimary == true,
                isHighlighted = status?.isHighlighted == true,
                isComposite = rule.ceilingType == CeilingType.COMPOSITE
            )
        }
    }

    private fun rebuildCeilingBlocks(
        machine: Machine?,
        currentBlocks: List<CeilingBlockUiModel>
    ): List<CeilingBlockUiModel> {
        if (machine == null) return currentBlocks
        val currentInputs = currentBlocks.associate { it.ruleKey to it.currentValue }
        val statusesByRuleKey = calculateCeilingStatusUseCase(
            machine = machine,
            currentInputs = currentInputs
        ).statuses.associateBy { it.ruleKey }

        return currentBlocks.map { block ->
            val status = statusesByRuleKey[block.ruleKey] ?: return@map block
            val ceilingType = machine.ceilingRules.firstOrNull { it.ruleKey == block.ruleKey }?.ceilingType
            val limitText = formatValue(status.limitValue, status.unit, ceilingType)
            val remainText = if (status.remainValue <= 0) {
                "到達済み"
            } else {
                formatValue(status.remainValue, status.unit, ceilingType)
            }
            val currentText = formatValue(status.currentValue, status.unit, ceilingType)
            val summary = if (remainText == "到達済み") {
                "現在 $currentText / 到達済み"
            } else {
                "現在 $currentText / 残り $remainText"
            }

            block.copy(
                currentValue = status.currentValue,
                currentText = currentText,
                limitText = limitText,
                remainText = remainText,
                summaryText = summary,
                isPrimary = status.isPrimary,
                isHighlighted = status.isHighlighted
            )
        }
    }

    private fun buildGuidanceText(machine: Machine?): String {
        if (machine == null) return ""

        return when (machine.type?.uppercase()) {
            "AT" -> "天井条件ごとに入力欄と恩恵をまとめて確認できます。"
            "A", "A+ART", "A+AT" -> "ボーナス回数を中心に入力して、総ゲーム数とのバランスを見てください。"
            else -> "機種ごとの設定差がある項目だけ表示しています。"
        }
    }

    private fun buildRateText(
        counterKey: String,
        totalGames: Int,
        count: Int?
    ): String? {
        if (counterKey == TOTAL_GAMES_KEY) return null
        if (count == null || count <= 0 || totalGames <= 0) return null
        return "1/${"%.1f".format(totalGames.toDouble() / count.toDouble())}"
    }

    private fun buildCeilingSteps(rule: CeilingRule): List<Int> {
        val base = rule.stepValue.coerceAtLeast(1)
        val rawSteps = when (rule.ceilingType) {
            CeilingType.GAME -> listOf(1, base, base * 5, base * 10)
            CeilingType.COUNT -> listOf(1, 2, 3)
            CeilingType.CYCLE -> listOf(1, 2, 5)
            CeilingType.POINT -> {
                val pointBase = base.coerceAtLeast(10)
                listOf(pointBase, pointBase * 5, pointBase * 10)
            }
            CeilingType.COMPOSITE -> emptyList()
        }

        return rawSteps.filter { it > 0 }.distinct().sorted()
    }

    private fun buildSupportNote(rule: CeilingRule): String? {
        return when {
            rule.ceilingType == CeilingType.COMPOSITE -> "複合条件は現在のUIでは個別入力未対応です。JSON定義を保持したまま、今後の自動判定対応に備えています。"
            !rule.description.isNullOrBlank() -> rule.description
            else -> null
        }
    }

    private fun formatValue(value: Int, unit: String, ceilingType: CeilingType?): String {
        val normalizedUnit = when {
            unit.isNotBlank() -> unit
            ceilingType == CeilingType.COUNT || ceilingType == CeilingType.CYCLE -> "回"
            ceilingType == CeilingType.POINT -> "pt"
            else -> "G"
        }
        return "$value$normalizedUnit"
    }

    private fun getCounterValue(session: PlaySession, key: String): Int {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue ?: 0
    }

    private fun getCounterValueOrNull(session: PlaySession, key: String): Int? {
        return session.counters.firstOrNull { it.counterKey == key }?.intValue
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

    private fun ceilingInputStorageKey(ruleKey: String): String = "ceiling::$ruleKey"

    companion object {
        private const val TOTAL_GAMES_KEY = "total_games"
    }
}
