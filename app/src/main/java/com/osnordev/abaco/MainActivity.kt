package com.osnordev.abaco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dev.korelibrary.themes.KoreTheme
import com.osnordev.abaco.ui.MainScaffold
import com.osnordev.abaco.ui.MainViewModel
import com.osnordev.abaco.ui.screens.clients.ClientFormScreen
import com.osnordev.abaco.ui.screens.clients.ClientListScreen
import com.osnordev.abaco.ui.screens.onboarding.OnboardingScreen
import com.osnordev.abaco.ui.screens.pin.PinScreen
import com.osnordev.abaco.ui.theme.AbacoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The primary entry point for the application.
 *
 * Handles top-level navigation: onboarding → PIN → client selector → main app.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbacoTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()
                val pinUnlocked by mainViewModel.pinUnlocked.collectAsState()
                val activeClientId by mainViewModel.activeClientId.collectAsState()

                when {
                    onboardingCompleted == null -> { /* splash / loading */ }
                    onboardingCompleted == false -> OnboardingScreen(
                        onFinish = { mainViewModel.completeOnboarding() }
                    )
                    !pinUnlocked -> PinScreen(
                        onUnlocked = { mainViewModel.onPinUnlocked() }
                    )
                    activeClientId == null -> {
                        // Mini NavHost solo para el flujo de selección/creación de cliente
                        val clientNavController = rememberNavController()
                        NavHost(
                            navController = clientNavController,
                            startDestination = "client_list"
                        ) {
                            composable("client_list") {
                                ClientListScreen(
                                    onAddClient = { clientNavController.navigate("client_form/-1") },
                                    onEditClient = { id -> clientNavController.navigate("client_form/$id") },
                                    onClientSelected = { /* activeClientId cambiará y saldrá de este bloque */ }
                                )
                            }
                            composable(
                                route = "client_form/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != -1L }
                                ClientFormScreen(
                                    clientId = id,
                                    onNavigateBack = { clientNavController.popBackStack() }
                                )
                            }
                        }
                    }
                    else -> MainScaffold(viewModel = mainViewModel)
                }
            }
        }
    }
}
