package com.example.slotanalyzer.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.domain.model.MachineFilter
import com.example.slotanalyzer.domain.model.MachineSortOrder
import com.example.slotanalyzer.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSectionCard(title = "表示設定") {
                Text(
                    text = "テーマ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SettingsRadioRow(
                    label = "端末設定に従う",
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                )
                SettingsRadioRow(
                    label = "ライト",
                    selected = state.themeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                )
                SettingsRadioRow(
                    label = "ダーク",
                    selected = state.themeMode == ThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                )

                SettingsSwitchRow(
                    label = "天井情報を表示",
                    checked = state.showCeiling,
                    onCheckedChange = viewModel::setShowCeiling
                )
                SettingsSwitchRow(
                    label = "外部リンクを表示",
                    checked = state.showExternalLinks,
                    onCheckedChange = viewModel::setShowExternalLinks
                )
            }

            SettingsSectionCard(title = "機種一覧") {
                Text(
                    text = "並び順",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SettingsRadioRow(
                    label = "リリース日順",
                    selected = state.machineSortOrder == MachineSortOrder.RELEASE_DATE,
                    onClick = { viewModel.setMachineSortOrder(MachineSortOrder.RELEASE_DATE) }
                )
                SettingsRadioRow(
                    label = "名前順",
                    selected = state.machineSortOrder == MachineSortOrder.NAME,
                    onClick = { viewModel.setMachineSortOrder(MachineSortOrder.NAME) }
                )

                Text(
                    text = "表示対象",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )

                SettingsRadioRow(
                    label = "すべて",
                    selected = state.machineFilter == MachineFilter.ALL,
                    onClick = { viewModel.setMachineFilter(MachineFilter.ALL) }
                )
                SettingsRadioRow(
                    label = "AT / スマスロ",
                    selected = state.machineFilter == MachineFilter.AT_SMART,
                    onClick = { viewModel.setMachineFilter(MachineFilter.AT_SMART) }
                )
                SettingsRadioRow(
                    label = "ノーマル",
                    selected = state.machineFilter == MachineFilter.NORMAL,
                    onClick = { viewModel.setMachineFilter(MachineFilter.NORMAL) }
                )
                SettingsRadioRow(
                    label = "沖スロ / ハナハナ",
                    selected = state.machineFilter == MachineFilter.OKINAWA,
                    onClick = { viewModel.setMachineFilter(MachineFilter.OKINAWA) }
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}