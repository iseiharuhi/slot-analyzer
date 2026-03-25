package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.slotanalyzer.feature.session.presentation.CeilingBlockUiModel

@Composable
fun CeilingBlockCard(
    item: CeilingBlockUiModel,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable(item.ruleKey) { mutableStateOf(false) }

    val containerModifier = if (item.isHighlighted) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f))
            .padding(1.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = modifier.then(containerModifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (expanded) {
                if (item.inputVisible) {
                    NumericAdjustField(
                        label = if (item.unit.isBlank()) "現在値" else "現在値 (${item.unit})",
                        value = item.currentValue,
                        onValueChange = { onValueChange(it ?: 0) },
                        steps = item.steps
                    )
                } else {
                    Text(
                        text = if (item.isComposite) {
                            "この天井条件は複合条件のため、現状は入力UI未対応です。"
                        } else {
                            "この天井条件は入力不要です。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DetailLine(label = "現在", value = item.currentText)
                DetailLine(label = "天井", value = item.limitText)
                DetailLine(label = "残り", value = item.remainText)

                item.benefitText?.takeIf { it.isNotBlank() }?.let { benefit ->
                    DetailLine(label = "恩恵", value = benefit)
                }

                item.resetText?.takeIf { it.isNotBlank() }?.let { reset ->
                    DetailLine(label = "リセット時", value = reset)
                }

                item.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
