package com.example.slotanalyzer.feature.inference.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.slotanalyzer.core.ui.component.BarSection
import com.example.slotanalyzer.core.ui.component.CeilingStatusSection
import com.example.slotanalyzer.core.ui.component.ConfidenceChip
import com.example.slotanalyzer.core.ui.component.TopSettingCard
import com.example.slotanalyzer.core.ui.component.extractTopSettingLabel

@Composable
fun InferenceScreen(
    navController: NavController,
    sessionId: String,
    viewModel: InferenceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val topSettingLabel = extractTopSettingLabel(state.topSettingText)

    LaunchedEffect(sessionId) {
        viewModel.loadInference(sessionId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "推測結果",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "機種: ${state.machineName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (state.machineTypeText.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("タイプ: ${state.machineTypeText}") }
                    )
                }

                if (state.probabilityModeText.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(state.probabilityModeText) }
                    )
                }

                ConfidenceChip(state.confidenceText)

                Text(
                    text = state.finishedStatusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.topSettingText.isNotBlank()) {
                    TopSettingCard(state.topSettingText)
                }

                if (state.candidateSummaryText.isNotBlank()) {
                    Text(
                        text = "上位候補: ${state.candidateSummaryText}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = state.summary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.settingDistributionPoints.isNotEmpty()) {
            SettingTrendCard(points = state.settingDistributionPoints)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "入力状況",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                InfoRow(label = "現在ハマり", value = "${state.currentGameCount} G")

                state.inputItems
                    .groupBy { it.categoryLabel }
                    .forEach { (category, items) ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        items.forEach { item ->
                            InfoRow(label = item.label, value = item.valueText)
                        }
                    }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CeilingStatusSection(
            items = state.ceilingItems,
            emptyText = "この機種には表示可能な天井情報がありません。"
        )

        if (state.reasonItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "推測理由",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (state.reasonSummaryText.isNotBlank()) {
                        Text(
                            text = state.reasonSummaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.reasonItems.forEach { item ->
                        ReasonCard(item)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        BarSection(
            title = "設定別推測スコア",
            items = state.settingBars,
            highlightedLabel = topSettingLabel
        )

        Spacer(modifier = Modifier.height(16.dp))
        BarSection(
            title = "設定帯評価",
            items = state.bandBars,
            highlightedLabel = null
        )

        state.finishMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            MessageCard(message)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.finishSession() },
                enabled = !state.isFinished && !state.isFinishing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        state.isFinishing -> "終了処理中..."
                        state.isFinished -> "実戦終了済み"
                        else -> "実戦終了"
                    }
                )
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("戻る")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingTrendCard(points: List<SettingDistributionPointUiModel>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = Color.LightGray
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "設定分布グラフ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "設定1〜6の推測比率を折れ線で確認できます。",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (points.isEmpty()) return@Canvas

                val leftPadding = 36.dp.toPx()
                val bottomPadding = 24.dp.toPx()
                val topPadding = 12.dp.toPx()
                val rightPadding = 12.dp.toPx()

                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding
                val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f

                for (i in 0..4) {
                    val y = topPadding + (chartHeight / 4f) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = leftPadding + stepX * index
                    val y = topPadding + chartHeight * (1f - point.value.coerceIn(0f, 1f))
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                points.forEachIndexed { index, point ->
                    val x = leftPadding + stepX * index
                    val y = topPadding + chartHeight * (1f - point.value.coerceIn(0f, 1f))
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { point ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${(point.value * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonCard(item: InferenceReasonUiModel) {
    val containerColor = when (item.levelKey) {
        "strong" -> MaterialTheme.colorScheme.primaryContainer
        "positive" -> MaterialTheme.colorScheme.tertiaryContainer
        "contradiction" -> MaterialTheme.colorScheme.errorContainer
        "weak" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val badgeColor = when (item.levelKey) {
        "strong" -> MaterialTheme.colorScheme.primary
        "positive" -> MaterialTheme.colorScheme.tertiary
        "contradiction" -> MaterialTheme.colorScheme.error
        "weak" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.secondary
    }

    val badgeTextColor = when (item.levelKey) {
        "weak" -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.onPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (item.levelLabel.isNotBlank()) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = item.levelLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = item.valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = item.evaluationText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}