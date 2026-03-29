package com.osnordev.abaco.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.osnordev.abaco.domain.model.AppModule

@Composable
fun BottomNavBar(
    navController: NavController,
    activeModules: Set<AppModule>
) {
    // Taxes tab is only visible when TAX_ONAT module is active
    val visibleItems = bottomNavItems.filter { item ->
        when (item.screen) {
            Screen.Taxes -> activeModules.contains(AppModule.TAX_ONAT)
            else -> true
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        visibleItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            // Pop up to the start destination to avoid building a large back stack
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
