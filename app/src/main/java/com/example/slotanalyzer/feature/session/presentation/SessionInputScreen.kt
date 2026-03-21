package com.example.slotanalyzer.feature.session.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slotanalyzer.feature.session.presentation.component.NumericAdjustField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionInputScreen(
    onMoveToInference: () -> Unit,
    onBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("実戦入力") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.machineName.isNotBlank()) {
                        Text(
                            text = "機種: ${state.machineName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "総回転数は ±1000、その他は ±100 まで対応",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "数値は直接入力もできます",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            NumericAdjustField(
                label = "総回転数",
                value = state.totalGames,
                onValueChange = viewModel::setTotalGames,
                steps = listOf(1, 10, 100, 1000)
            )

            NumericAdjustField(
                label = "BIG回数",
                value = state.bigCount,
                onValueChange = viewModel::setBigCount,
                steps = listOf(1, 10, 100)
            )

            NumericAdjustField(
                label = "REG回数",
                value = state.regCount,
                onValueChange = viewModel::setRegCount,
                steps = listOf(1, 10, 100)
            )

            NumericAdjustField(
                label = "CZ回数",
                value = state.czCount,
                onValueChange = viewModel::setCzCount,
                steps = listOf(1, 10, 100)
            )

            NumericAdjustField(
                label = "AT回数",
                value = state.atCount,
                onValueChange = viewModel::setAtCount,
                steps = listOf(1, 10, 100)
            )

            Button(
                onClick = onMoveToInference,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("推測結果へ")
            }
        }
    }
}