package com.example.slotanalyzer.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.slotanalyzer.feature.history.detail.presentation.HistoryDetailScreen
import com.example.slotanalyzer.feature.history.list.presentation.HistoryListScreen
import com.example.slotanalyzer.feature.home.presentation.HomeScreen
import com.example.slotanalyzer.feature.inference.presentation.InferenceScreen
import com.example.slotanalyzer.feature.machine.presentation.MachineSelectScreen
import com.example.slotanalyzer.feature.session.presentation.SessionInputScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(AppRoutes.MACHINE_SELECT) {
            MachineSelectScreen(navController = navController)
        }

        composable(
            route = AppRoutes.SESSION_INPUT,
            arguments = listOf(
                navArgument("machineId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            SessionInputScreen(
                onMoveToInference = {
                    navController.navigate(AppRoutes.INFERENCE)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.INFERENCE) {
            InferenceScreen(navController = navController)
        }

        composable(AppRoutes.HISTORY_LIST) {
            HistoryListScreen(navController = navController)
        }

        composable(
            route = AppRoutes.HISTORY_DETAIL,
            arguments = listOf(
                navArgument("historyId") {
                    type = NavType.StringType
                }
            )
        ) {
            HistoryDetailScreen(navController = navController)
        }
    }
}