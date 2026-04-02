package com.osnordev.abaco.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.osnordev.abaco.ui.navigation.AppNavHost
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
    val userProfile by viewModel.userProfile.collectAsState()
    val showPinSetup by viewModel.showPinSetup.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = isDarkThemePref ?: systemDark

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Track current route for drawer item highlighting
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Settings panel state (profile, theme, PIN)
    var showSettingsPanel by remember { mutableStateOf(false) }
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()

    AbacoTheme(darkTheme = isDark) {
        // PIN setup dialog
        if (showPinSetup) {
            PinSetupDialog(
                onDismiss = { viewModel.onPinSetupDismissed() },
                onConfirm = { pin -> viewModel.setupPin(pin) }
            )
        }

        // Settings panel (right-side overlay for profile/theme/PIN)
        if (showSettingsPanel) {
            SettingsPanelDialog(
                isDarkTheme = isDark,
                isPinEnabled = viewModel.pinEnabled,
                isNotificationsEnabled = isNotificationsEnabled,
                userProfile = userProfile,
                onThemeToggle = { viewModel.setDarkTheme(it) },
                onPinToggle = { viewModel.togglePin() },
                onNotificationsToggle = { viewModel.setNotificationsEnabled(it) },
                onProfileSave = { viewModel.saveProfile(it) },
                onDismiss = { showSettingsPanel = false }
            )
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    AppDrawerContent(
                        currentRoute = currentRoute,
                        userProfile = userProfile,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = drawerItems.find { it.screen.route == currentRoute }?.label ?: "Ábaco",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menú")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSettingsPanel = true }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil y configuración")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    activeModules = activeModules,
                    modifier = Modifier.padding(innerPadding)
                )
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
                Text("Elige un PIN de 4 dígitos para proteger la app.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { pin = it; error = null } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { confirmPin = it; error = null } },
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
