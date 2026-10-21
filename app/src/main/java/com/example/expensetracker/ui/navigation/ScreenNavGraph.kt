package com.example.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expensetracker.ui.addedit.AddEditTransactionScreen
import com.example.expensetracker.ui.history.TransactionHistoryScreen
import com.example.expensetracker.ui.home.HomeScreen
import com.example.expensetracker.ui.statistics.StatisticsScreen

@Composable
fun ScreenNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddEditTransaction.createRoute())
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(Screen.History.route) {
            TransactionHistoryScreen(
                onItemClick = { transactionId ->
                    navController.navigate(Screen.AddEditTransaction.createRoute(transactionId))
                }
            )
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }
        composable(
            route = Screen.AddEditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val transactionIdArg = backStackEntry.arguments?.getLong("transactionId")
            val transactionId = if (transactionIdArg == -1L) null else transactionIdArg
            AddEditTransactionScreen(
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
