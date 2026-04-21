package com.example.slotanalyzer.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.domain.model.MachineFilterKeys
import com.example.slotanalyzer.domain.model.MachineSortOrder
import com.example.slotanalyzer.domain.model.ThemeMode
import androidx.compose.ui.unit.dp

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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSectionCard(title = "表示設定") {
                SettingsSelectorField(
                    label = "テーマ",
                    value = when (state.themeMode) {
                        ThemeMode.LIGHT -> "ライト"
                        ThemeMode.DARK -> "ダーク"
                        else -> "端末設定に従う"
                    },
                    options = listOf(
                        SelectorOption("端末設定に従う") { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        SelectorOption("ライト") { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        SelectorOption("ダーク") { viewModel.setThemeMode(ThemeMode.DARK) }
                    )
                )

                SettingsSelectorField(
                    label = "天井情報",
                    value = if (state.showCeiling) "表示" else "非表示",
                    options = listOf(
                        SelectorOption("表示") { viewModel.setShowCeiling(true) },
                        SelectorOption("非表示") { viewModel.setShowCeiling(false) }
                    )
                )

                SettingsSelectorField(
                    label = "外部リンク",
                    value = if (state.showExternalLinks) "表示" else "非表示",
                    options = listOf(
                        SelectorOption("表示") { viewModel.setShowExternalLinks(true) },
                        SelectorOption("非表示") { viewModel.setShowExternalLinks(false) }
                    )
                )
            }

            SettingsSectionCard(title = "機種一覧") {
                SettingsSelectorField(
                    label = "並び順",
                    value = if (state.machineSortOrder == MachineSortOrder.NAME) "名前順" else "リリース日順",
                    options = listOf(
                        SelectorOption("リリース日順") {
                            viewModel.setMachineSortOrder(MachineSortOrder.RELEASE_DATE)
                        },
                        SelectorOption("名前順") { viewModel.setMachineSortOrder(MachineSortOrder.NAME) }
                    )
                )

                Text(
                    text = "表示対象",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                SettingsCheckboxRow(
                    label = "すべて表示",
                    checked = state.isAllTypesSelected,
                    onCheckedChange = viewModel::setShowAllTypes
                )
                SettingsCheckboxRow(
                    label = "AT / スマスロ",
                    checked = state.selectedMachineFilters.contains(MachineFilterKeys.AT_SMART),
                    onCheckedChange = { viewModel.toggleMachineFilter(MachineFilterKeys.AT_SMART) }
                )
                SettingsCheckboxRow(
                    label = "ノーマル",
                    checked = state.selectedMachineFilters.contains(MachineFilterKeys.NORMAL),
                    onCheckedChange = { viewModel.toggleMachineFilter(MachineFilterKeys.NORMAL) }
                )
                SettingsCheckboxRow(
                    label = "沖スロ / ハナハナ",
                    checked = state.selectedMachineFilters.contains(MachineFilterKeys.OKINAWA),
                    onCheckedChange = { viewModel.toggleMachineFilter(MachineFilterKeys.OKINAWA) }
                )
            }
        }
    }
}

private data class SelectorOption(
    val label: String,
    val onSelect: () -> Unit
)

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun SettingsSelectorField(
    label: String,
    value: String,
    options: List<SelectorOption>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        CompactSelectorBox(
            value = value,
            onClick = { expanded = true }
        )
    }

    if (expanded) {
        CompactSelectionDialog(
            title = label,
            selectedValue = value,
            options = options,
            onDismiss = { expanded = false }
        )
    }
}

@Composable
private fun CompactSelectorBox(
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactSelectionDialog(
    title: String,
    selectedValue: String,
    options: List<SelectorOption>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                options.forEachIndexed { index, option ->
                    CompactDialogOptionRow(
                        label = option.label,
                        selected = option.label == selectedValue,
                        onClick = {
                            option.onSelect()
                            onDismiss()
                        }
                    )
                    if (index != options.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDialogOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "選択中",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingsCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
