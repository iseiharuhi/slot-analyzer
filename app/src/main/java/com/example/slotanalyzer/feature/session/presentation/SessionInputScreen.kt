
package com.example.slotanalyzer.feature.session.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.slotanalyzer.feature.session.presentation.component.CeilingSectionCard
import com.example.slotanalyzer.domain.model.CeilingCondition

@Composable
fun SessionInputScreen(
    viewModel: SessionInputViewModel,
    conditions: List<CeilingCondition>
) {
    val ceilingValues by viewModel.ceilingInputs.collectAsState()

    CeilingSectionCard(
        conditions = conditions,
        values = ceilingValues,
        onValueChange = { id, v ->
            viewModel.updateCeiling(id, v)
        }
    )
}
