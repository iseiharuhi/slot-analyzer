package com.example.slotanalyzer.feature.session.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.slotanalyzer.core.navigation.AppRoutes
import com.example.slotanalyzer.core.util.RateFormatter

@Composable
fun SessionInputScreen(
    navController: NavController,
    machineId: String,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "実戦入力画面",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "機種: ${uiState.machineName.ifBlank { machineId }}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        CounterSection(
            title = "総ゲーム数",
            value = uiState.totalGames,
            onMinusClick = { viewModel.decreaseTotalGames() },
            onPlusClick = { viewModel.increaseTotalGames() },
            minusLabel = "-100",
            plusLabel = "+100"
        )

        Spacer(modifier = Modifier.height(20.dp))

        CounterSection(
            title = "BIG",
            value = uiState.bigCount,
            onMinusClick = { viewModel.decreaseBigCount() },
            onPlusClick = { viewModel.increaseBigCount() },
            minusLabel = "-1",
            plusLabel = "+1"
        )

        Spacer(modifier = Modifier.height(20.dp))

        CounterSection(
            title = "REG",
            value = uiState.regCount,
            onMinusClick = { viewModel.decreaseRegCount() },
            onPlusClick = { viewModel.increaseRegCount() },
            minusLabel = "-1",
            plusLabel = "+1"
        )

        Spacer(modifier = Modifier.height(20.dp))

        CounterSection(
            title = "CZ",
            value = uiState.czCount,
            onMinusClick = { viewModel.decreaseCzCount() },
            onPlusClick = { viewModel.increaseCzCount() },
            minusLabel = "-1",
            plusLabel = "+1"
        )

        Spacer(modifier = Modifier.height(20.dp))

        CounterSection(
            title = "AT",
            value = uiState.atCount,
            onMinusClick = { viewModel.decreaseAtCount() },
            onPlusClick = { viewModel.increaseAtCount() },
            minusLabel = "-1",
            plusLabel = "+1"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "BIG確率: ${RateFormatter.calculateRateText(uiState.totalGames, uiState.bigCount)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "REG確率: ${RateFormatter.calculateRateText(uiState.totalGames, uiState.regCount)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(AppRoutes.INFERENCE) }) {
            Text("推測結果を見る")
        }
    }
}

@Composable
fun CounterSection(
    title: String,
    value: Int,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    minusLabel: String,
    plusLabel: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$title : $value",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onMinusClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(minusLabel)
            }

            Button(
                onClick = onPlusClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(plusLabel)
            }
        }
    }
}