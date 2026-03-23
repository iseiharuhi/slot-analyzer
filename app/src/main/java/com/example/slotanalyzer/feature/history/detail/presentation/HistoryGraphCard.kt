
package com.example.slotanalyzer.feature.history.detail.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HistoryGraphCard(gamePoints: List<Int>) {

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = Color.LightGray

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "実戦履歴グラフ",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (gamePoints.isEmpty()) return@Canvas

                val max = gamePoints.maxOrNull()?.toFloat() ?: 1f
                val min = gamePoints.minOrNull()?.toFloat() ?: 0f

                val left = 40.dp.toPx()
                val bottom = 20.dp.toPx()
                val top = 10.dp.toPx()
                val right = 10.dp.toPx()

                val width = size.width - left - right
                val height = size.height - top - bottom

                val stepX = if (gamePoints.size > 1) width / (gamePoints.size - 1) else 0f

                // grid
                for (i in 0..4) {
                    val y = top + height * i / 4f
                    drawLine(
                        color = gridColor,
                        start = Offset(left, y),
                        end = Offset(size.width - right, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                var prev: Offset? = null

                gamePoints.forEachIndexed { i, v ->
                    val x = left + stepX * i
                    val normalized = if (max - min == 0f) 0f else (v - min) / (max - min)
                    val y = top + height * (1f - normalized)

                    val current = Offset(x, y)

                    prev?.let {
                        drawLine(
                            color = lineColor,
                            start = it,
                            end = current,
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = current
                    )

                    prev = current
                }
            }
        }
    }
}
