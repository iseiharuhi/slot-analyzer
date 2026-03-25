package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.slotanalyzer.domain.model.CeilingCondition
import com.example.slotanalyzer.domain.model.InputMode

@Composable
fun CeilingConditionCard(
    condition: CeilingCondition,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = value?.let { condition.ceilingValue - it }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = condition.title,
                style = MaterialTheme.typography.titleMedium
            )

            if (condition.showInput) {
                when (condition.inputMode) {
                    InputMode.TEXT -> TextInput(value = value, onValueChange = onValueChange)
                    InputMode.STEPPER -> StepperInput(
                        value = value,
                        step = condition.stepValue,
                        onValueChange = onValueChange
                    )
                    InputMode.READ_ONLY -> Unit
                }
            }

            Text(text = "天井値: ${condition.ceilingValue}")
            Text(
                text = when {
                    value == null -> "残り: ー"
                    remaining != null && remaining <= 0 -> "到達済み"
                    else -> "残り: $remaining"
                }
            )

            condition.benefitText?.let { Text(text = "恩恵: $it") }
            condition.resetText?.let { Text(text = "リセット: $it") }
        }
    }
}
