package com.spendmindai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spendmindai.app.features.bankstatement.BankStatementImportScreen
import com.spendmindai.app.features.onboarding.OnboardingScreen
import com.spendmindai.app.features.premium.PremiumScreen
import com.spendmindai.app.features.record.MainScreen
import com.spendmindai.app.features.record.ManualExpenseScreen
import com.spendmindai.app.features.settings.CurrencySelectionScreen
import com.spendmindai.app.features.settings.DataManagementScreen
import com.spendmindai.app.features.settings.LanguageSelectionScreen
import com.spendmindai.app.features.settings.RecurringExpenseScreen
import com.spendmindai.app.features.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object ManualExpense : Screen("manual_expense")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
    object Premium : Screen("premium")
    object BankImport : Screen("bank_import")
    object CurrencySelection : Screen("currency_selection")
    object LanguageSelection : Screen("language_selection")
    object RecurringExpenses : Screen("recurring_expenses")
    object DataManagement : Screen("data_management")
}

@Composable
fun SpendMindNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                onNavigateToManualExpense = { navController.navigate(Screen.ManualExpense.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToBankImport = { navController.navigate(Screen.BankImport.route) }
            )
        }

        composable(Screen.ManualExpense.route) {
            ManualExpenseScreen(
                onClose = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BankImport.route) {
            BankStatementImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewHistory = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.BankImport.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CurrencySelection.route) {
            CurrencySelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RecurringExpenses.route) {
            RecurringExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DataManagement.route) {
            DataManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
