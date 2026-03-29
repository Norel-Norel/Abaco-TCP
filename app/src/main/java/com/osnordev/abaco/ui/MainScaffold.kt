package com.osnordev.abaco.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.osnordev.abaco.ui.navigation.AppNavHost
import com.osnordev.abaco.ui.navigation.BottomNavBar
import com.osnordev.abaco.ui.navigation.Screen
import com.osnordev.abaco.ui.theme.AbacoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activeModules by viewModel.activeModules.collectAsState()
    val isDarkThemePref by viewModel.isDarkTheme.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val showPinSetup by viewModel.showPinSetup.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = isDarkThemePref ?: systemDark

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDrawerOpen = drawerState.isOpen

    AbacoTheme(darkTheme = isDark) {
        // PIN setup dialog — shown when user enables PIN from the drawer
        if (showPinSetup) {
            PinSetupDialog(
                onDismiss = { viewModel.onPinSetupDismissed() },
                onConfirm = { pin -> viewModel.setupPin(pin) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Ábaco") },
                        actions = {
                            // Opens the right-side settings drawer (Req 13.2)
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Configuración")
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        navController = navController,
                        activeModules = activeModules
                    )
                }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    activeModules = activeModules,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Scrim — dims the background when drawer is open (Req 13.5)
            AnimatedVisibility(
                visible = isDrawerOpen,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { scope.launch { drawerState.close() } }
                )
            }

            // Right-side drawer panel (Req 13.2)
            AnimatedVisibility(
                visible = isDrawerOpen,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = androidx.compose.ui.unit.Dp(8f)
                ) {
                    SettingsDrawerContent(
                        isDarkTheme = isDark,
                        isPinEnabled = viewModel.pinEnabled,
                        isNotificationsEnabled = isNotificationsEnabled,
                        userProfile = userProfile,
                        onThemeToggle = { dark -> viewModel.setDarkTheme(dark) },
                        onPinToggle = {
                            viewModel.togglePin()
                            scope.launch { drawerState.close() }
                        },
                        onNotificationsToggle = { viewModel.setNotificationsEnabled(it) },
                        onProfileSave = { viewModel.saveProfile(it) },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        },
                        onNavigateToQr = {
                            navController.navigate(Screen.QrCode.route) { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar PIN") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text("Elige un PIN de 4 a 6 dígitos para proteger la app.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { pin = it; error = null } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { confirmPin = it; error = null } },
                    label = { Text("Confirmar PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin.length < 4 -> error = "El PIN debe tener al menos 4 dígitos"
                    pin != confirmPin -> error = "Los PINs no coinciden"
                    else -> onConfirm(pin)
                }
            }) { Text("Activar PIN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
