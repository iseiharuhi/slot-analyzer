package com.example.slotanalyzer.feature.machine.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.slotanalyzer.core.navigation.AppRoutes

@Composable
fun MachineSelectScreen(
    navController: NavController,
    viewModel: MachineSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedMachineId by rememberSaveable { mutableStateOf<String?>(null) }

    val normalizedQuery = searchQuery.trim()
    val filteredMachines = state.machines.filter { machine ->
        normalizedQuery.isBlank() ||
            machine.name.contains(normalizedQuery, ignoreCase = true) ||
            machine.rawName.contains(normalizedQuery, ignoreCase = true) ||
            machine.name.replace(" ", "").contains(
                normalizedQuery.replace(" ", ""),
                ignoreCase = true
            ) ||
            machine.rawName.replace(" ", "").contains(
                normalizedQuery.replace(" ", ""),
                ignoreCase = true
            )
    }

    val selectedMachine = filteredMachines.firstOrNull { it.id == selectedMachineId }
        ?: state.machines.firstOrNull { it.id == selectedMachineId }

    LaunchedEffect(state.machines, selectedMachineId) {
        if (selectedMachineId != null && state.machines.none { it.id == selectedMachineId }) {
            selectedMachineId = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "機種を選択",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "並び順: ${state.sortOrderLabel} / 表示対象: ${state.filterLabel}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            label = { Text("機種名で検索") }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredMachines,
                key = { it.id }
            ) { machine ->
                val isSelected = machine.id == selectedMachineId

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedMachineId = machine.id
                            viewModel.onMachineSelected(machine)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = machine.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (filteredMachines.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "該当する機種がありません",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selectedMachine != null) {
            Text(
                text = "選択中: ${selectedMachine.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            Button(
                onClick = {
                    viewModel.startNewSession(selectedMachine) { sessionId ->
                        navController.navigate(AppRoutes.sessionInput(sessionId))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("新規実戦開始")
            }

            OutlinedButton(
                onClick = {
                    viewModel.resumeSession(selectedMachine) { sessionId ->
                        navController.navigate(AppRoutes.sessionInput(sessionId))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("続きから再開")
            }
        }

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.SETTINGS) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("設定を開く")
        }

        OutlinedButton(
            onClick = {
                navController.navigate(AppRoutes.HOME) {
                    popUpTo(AppRoutes.HOME) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ホームへ戻る")
        }
    }
}
