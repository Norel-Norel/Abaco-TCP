package com.osnordev.abaco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.ui.MainScaffold
import com.osnordev.abaco.ui.MainViewModel
import com.osnordev.abaco.ui.screens.onboarding.OnboardingScreen
import com.osnordev.abaco.ui.screens.pin.PinScreen
import com.osnordev.abaco.ui.theme.AbacoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The primary entry point for the application.
 *
 * This activity handles the top-level navigation logic, observing state from [MainViewModel]
 * to determine whether to show onboarding, a security PIN screen, or the main application UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Configures the activity window and sets the Compose content.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbacoTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()
                val pinUnlocked by mainViewModel.pinUnlocked.collectAsState()

                when {
                    onboardingCompleted == null -> { /* splash / loading — render nothing */ }
                    onboardingCompleted == false -> OnboardingScreen(
                        onFinish = { mainViewModel.completeOnboarding() }
                    )
                    !pinUnlocked -> PinScreen(
                        onUnlocked = { mainViewModel.onPinUnlocked() }
                    )
                    else -> MainScaffold()
                }
            }
        }
    }
}
