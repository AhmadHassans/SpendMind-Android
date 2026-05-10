package com.spendmindai.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun HomeScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AddExpenseScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Add Expense Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ExpenseDetailScreenPlaceholder(expenseId: String, navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Expense Detail: $expenseId", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun CategoriesScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Categories Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun RecurringExpensesScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Recurring Expenses Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun SettingsScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Settings Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ReportsScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Reports Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun OnboardingScreenPlaceholder(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Onboarding Screen", style = MaterialTheme.typography.headlineMedium)
    }
}
