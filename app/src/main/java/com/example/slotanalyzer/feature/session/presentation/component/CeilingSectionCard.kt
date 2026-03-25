package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.slotanalyzer.domain.model.CeilingCondition

@Composable
fun CeilingSectionCard(
    conditions: List<CeilingCondition>,
    values: Map<String, Int?>,
    onValueChange: (String, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryConditions = conditions.filter { it.isPrimary }
    val collapsedList = if (primaryConditions.isNotEmpty()) primaryConditions else conditions.take(1)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("天井")
                Text(if (expanded) "▲" else "▼")
            }

            val targetList = if (expanded) {
                conditions.sortedBy { it.displayOrder }
            } else {
                collapsedList
            }

            targetList.forEach { condition ->
                CeilingConditionCard(
                    condition = condition,
                    value = values[condition.id],
                    onValueChange = { onValueChange(condition.id, it) }
                )
            }

            if (!expanded && conditions.size > collapsedList.size) {
                Text("その他${conditions.size - collapsedList.size}種類あり")
            }
        }
    }
}
