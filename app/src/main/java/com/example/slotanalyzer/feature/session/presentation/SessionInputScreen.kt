package com.example.slotanalyzer.feature.session.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.feature.session.presentation.component.NumericAdjustField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionInputScreen(
    sessionId: String,
    onMoveToInference: () -> Unit,
    onBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("入力値を初期化しますか？") },
            text = { Text("現在の実戦データをすべて0に戻します") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetAllCounters()
                    }
                ) {
                    Text("初期化する")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    val groupedItems = state.counterItems.groupBy { it.categoryLabel }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("実戦入力") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.machineName.isNotBlank()) {
                        Text(
                            text = state.machineName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (state.machineTypeText.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("タイプ: ${state.machineTypeText}") }
                        )
                    }

                    Text(
                        text = if (state.guidanceText.isBlank()) {
                            "機種ごとに必要な入力項目だけ表示しています。"
                        } else {
                            state.guidanceText
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "総回転数は 1 / 10 / 100 / 1000、その他は 1 / 10 / 100 で調整できます。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            groupedItems.forEach { (categoryLabel, items) ->
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        items.forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                NumericAdjustField(
                                    label = buildLabel(
                                        displayName = item.displayName,
                                        unit = item.unit,
                                        rateText = item.rateText
                                    ),
                                    value = item.value,
                                    onValueChange = { value -> viewModel.setCounter(item.key, value) },
                                    steps = if (item.key == "total_games") {
                                        listOf(1, 10, 100, 1000)
                                    } else {
                                        listOf(1, 10, 100)
                                    }
                                )

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
            }

            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("入力値を初期化")
            }

            Button(
                onClick = onMoveToInference,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("推測結果へ")
            }
        }
    }
}

private fun buildLabel(
    displayName: String,
    unit: String,
    rateText: String?
): String {
    val base = if (unit.isBlank()) displayName else "$displayName ($unit)"
    return if (rateText.isNullOrBlank()) base else "$base  確率: $rateText"
}
