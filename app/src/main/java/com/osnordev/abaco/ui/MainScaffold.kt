package com.osnordev.abaco.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    val activeClientName by viewModel.activeClientName.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = isDarkThemePref ?: systemDark

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showSettingsPanel by remember { mutableStateOf(false) }
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()

    AbacoTheme(darkTheme = isDark) {
        if (showPinSetup) {
            AlertDialog(
                onDismissRequest = { viewModel.onPinSetupDismissed() },
                title = { Text("Configurar PIN") },
                text = { Text("Introduce tu nuevo PIN de seguridad para proteger los datos de este cliente.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onPinSetupDismissed() }) { Text("OK") }
                }
            )
        }

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
                        activeClientName = activeClientName,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Principal.route) { saveState = true }
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
                            Column {
                                // drawerItems está definido en AppDrawer.kt en el mismo paquete com.osnordev.abaco.ui
                                val label = drawerItems.find { it.screen.route == currentRoute }?.label ?: "Ábaco"
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                activeClientName?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
