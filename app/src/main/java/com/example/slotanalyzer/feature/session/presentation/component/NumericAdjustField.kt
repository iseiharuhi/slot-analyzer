package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumericAdjustField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    steps: List<Int> = listOf(1, 10, 100)
) {
    val safeSteps = remember(steps) {
        steps.distinct().filter { it > 0 }.sorted()
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value.toString(),
            onValueChange = { input ->
                when {
                    input.isBlank() -> onValueChange(0)
                    input.all { it.isDigit() } -> onValueChange(input.toIntOrNull() ?: 0)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            label = { Text(label) }
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            safeSteps.sortedDescending().forEach { step ->
                AssistChip(
                    onClick = {
                        onValueChange((value - step).coerceAtLeast(0))
                    },
                    label = { Text("-$step") }
                )
            }

            safeSteps.forEach { step ->
                AssistChip(
                    onClick = {
                        onValueChange(value + step)
                    },
                    label = { Text("+$step") }
                )
            }
        }
    }
}