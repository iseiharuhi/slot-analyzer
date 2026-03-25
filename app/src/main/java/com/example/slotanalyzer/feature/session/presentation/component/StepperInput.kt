package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StepperInput(
    value: Int?,
    step: Int,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                val newValue = (value ?: 0) - step
                onValueChange(if (newValue <= 0) null else newValue)
            }
        ) {
            Text("-")
        }

        Text(
            text = value?.toString() ?: "ー",
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Button(
            onClick = {
                val newValue = (value ?: 0) + step
                onValueChange(newValue)
            }
        ) {
            Text("+")
        }
    }
}
