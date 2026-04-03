package com.example.slotanalyzer.feature.session.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.feature.session.presentation.component.CeilingBlockCard
import com.example.slotanalyzer.feature.settings.presentation.SettingsViewModel
import com.example.slotanalyzer.feature.session.presentation.component.NumericAdjustField

private enum class SessionInputTab(val label: String) {
    CEILING("天井確認"),
    COUNTER("実戦データ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionInputScreen(
    sessionId: String,
    onMoveToInference: () -> Unit,
    onBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userPreferences by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

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
    val tabs = buildList {
        if (userPreferences.showCeiling) add(SessionInputTab.CEILING)
        add(SessionInputTab.COUNTER)
    }

    LaunchedEffect(tabs.size) {
        if (selectedTabIndex > tabs.lastIndex) {
            selectedTabIndex = tabs.lastIndex.coerceAtLeast(0)
        }
    }

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
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
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

                    if (userPreferences.showExternalLinks) {
                        SiteLinksRow(
                            dmmUrl = state.dmmUrl,
                            ichigekiUrl = state.ichigekiUrl,
                            onOpenUrl = { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        )
                    }

                    if (state.machineTypeText.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("タイプ: ${state.machineTypeText}") }
                        )
                    }

                    if (state.settingStageText.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(state.settingStageText) }
                        )
                    }

                    Text(
                        text = if (state.guidanceText.isBlank()) {
                            "タブを切り替えて、実戦データ入力と天井確認を行えます。"
                        } else {
                            state.guidanceText
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (tabs.size > 1) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selectedTabIndex == index) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (tabs[selectedTabIndex]) {
                    SessionInputTab.CEILING -> {
                        if (state.ceilingBlocks.isEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "天井機能：非搭載",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            state.ceilingBlocks.forEach { block ->
                                CeilingBlockCard(
                                    item = block,
                                    onValueChange = { value ->
                                        viewModel.setCeilingInput(block.ruleKey, value)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    SessionInputTab.COUNTER -> {
                        groupedItems.forEach { (categoryLabel, items) ->
                            Card(modifier = Modifier.fillMaxWidth()) {
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
                                                },
                                                allowEmpty = item.allowEmpty
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
        }
    }
}

@Composable
private fun SiteLinksRow(
    dmmUrl: String?,
    ichigekiUrl: String?,
    onOpenUrl: (String) -> Unit
) {
    val hasAnyLink = !dmmUrl.isNullOrBlank() || !ichigekiUrl.isNullOrBlank()
    if (!hasAnyLink) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        dmmUrl?.takeIf { it.isNotBlank() }?.let { url ->
            Text(
                text = "DMM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { onOpenUrl(url) }
            )
        }

        ichigekiUrl?.takeIf { it.isNotBlank() }?.let { url ->
            Text(
                text = "1撃",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { onOpenUrl(url) }
            )
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
