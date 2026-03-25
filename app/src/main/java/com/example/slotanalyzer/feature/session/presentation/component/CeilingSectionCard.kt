package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.slotanalyzer.core.ui.model.CeilingStatusUiModel

@Composable
fun CeilingSectionCard(
    items: List<CeilingStatusUiModel>,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val representativeItem = items.firstOrNull { it.isPrimary } ?: items.firstOrNull()
    val visibleItems = if (expanded) items else representativeItem?.let { listOf(it) }.orEmpty()
    val hasExpandableContent = items.size > 1

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasExpandableContent) {
                        if (hasExpandableContent) {
                            expanded = !expanded
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "天井確認",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (hasExpandableContent) {
                    Text(
                        text = if (expanded) "▲" else "▼",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (items.isEmpty()) {
                Text(
                    text = "天井機能：非搭載",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            visibleItems.forEach { item ->
                CeilingConditionCard(item = item)
            }

            if (!expanded && hasExpandableContent) {
                Text(
                    text = "その他${items.size - 1}種類あり",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
