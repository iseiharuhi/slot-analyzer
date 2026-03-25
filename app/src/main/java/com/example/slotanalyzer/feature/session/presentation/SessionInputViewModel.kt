
package com.example.slotanalyzer.feature.session.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionInputViewModel : ViewModel() {

    private val _ceilingInputs = MutableStateFlow<Map<String, Int?>>(emptyMap())
    val ceilingInputs: StateFlow<Map<String, Int?>> = _ceilingInputs

    fun updateCeiling(conditionId: String, value: Int?) {
        _ceilingInputs.value =
            _ceilingInputs.value.toMutableMap().apply {
                put(conditionId, value)
            }
    }
}
