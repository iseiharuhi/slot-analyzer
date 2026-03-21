package com.example.slotanalyzer.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.slotanalyzer.core.ui.model.ScoreBarItem

@Composable
fun BarSection(
    title: String,
    items: List<ScoreBarItem>,
    highlightedLabel: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            items.forEachIndexed { index, item ->
                val isHighlighted = highlightedLabel != null && item.label == highlightedLabel

                ScoreBarRow(
                    item = item,
                    highlighted = isHighlighted
                )

                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "※相対的な推測スコアです",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ScoreBarRow(
    item: ScoreBarItem,
    highlighted: Boolean
) {
    val barColor = scoreBarColor(item.label)
    val trackColor = scoreTrackColor(barColor)

    val containerModifier = if (highlighted) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(modifier = containerModifier) {
        if (highlighted) {
            Text(
                text = "最有力",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(if (highlighted) 12.dp else 10.dp)
                        .height(if (highlighted) 12.dp else 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(barColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.label,
                    style = if (highlighted) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium
                )
            }

            Text(
                text = item.valueText,
                style = if (highlighted) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                fontWeight = FontWeight.ExtraBold,
                color = if (highlighted) MaterialTheme.colorScheme.primary else barColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { item.progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (highlighted) 14.dp else 12.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = if (highlighted) MaterialTheme.colorScheme.primary else barColor,
            trackColor = if (highlighted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
            } else {
                trackColor
            }
        )
    }
}

@Composable
fun ConfidenceChip(text: String) {
    val containerColor = when (text) {
        "高" -> MaterialTheme.colorScheme.tertiaryContainer
        "中" -> MaterialTheme.colorScheme.secondaryContainer
        "低" -> MaterialTheme.colorScheme.errorContainer
        "サンプル不足" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (text) {
        "高" -> MaterialTheme.colorScheme.onTertiaryContainer
        "中" -> MaterialTheme.colorScheme.onSecondaryContainer
        "低" -> MaterialTheme.colorScheme.onErrorContainer
        "サンプル不足" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = "信頼度: $text",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TopSettingCard(text: String) {
    if (text.isBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "最有力設定",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun scoreBarColor(label: String): Color {
    return when {
        label.contains("設定6") -> MaterialTheme.colorScheme.primary
        label.contains("設定5") -> MaterialTheme.colorScheme.tertiary
        label.contains("設定4") -> MaterialTheme.colorScheme.secondary
        label.contains("設定3") -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
        label.contains("設定2") -> MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
        label.contains("設定1") -> MaterialTheme.colorScheme.error
        label.contains("高設定帯") -> MaterialTheme.colorScheme.primary
        label.contains("中間設定帯") -> MaterialTheme.colorScheme.secondary
        label.contains("低設定帯") -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun scoreTrackColor(color: Color): Color = color.copy(alpha = 0.22f)

fun extractTopSettingLabel(topSettingText: String): String? {
    return Regex("""\d+""").find(topSettingText)?.value
}
