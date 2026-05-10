package com.spendmindai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.spendmindai.app.features.onboarding.OnboardingViewModel
import com.spendmindai.app.ui.navigation.Screen
import com.spendmindai.app.ui.navigation.SpendMindNavGraph
import com.spendmindai.app.ui.theme.SpendMindTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendMindTheme {
                val navController = rememberNavController()
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                val startDestination = if (onboardingViewModel.isOnboardingComplete) {
                    Screen.Main.route
                } else {
                    Screen.Onboarding.route
                }
                SpendMindNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
