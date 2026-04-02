package com.osnordev.abaco.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.osnordev.abaco.domain.model.AppModule
import com.osnordev.abaco.ui.screens.balance.BalanceSheetScreen
import com.osnordev.abaco.ui.screens.budget.BudgetScreen
import com.osnordev.abaco.ui.screens.charts.ChartsScreen
import com.osnordev.abaco.ui.screens.contacts.ContactFormScreen
import com.osnordev.abaco.ui.screens.contacts.ContactListScreen
import com.osnordev.abaco.ui.screens.dashboard.DashboardScreen
import com.osnordev.abaco.ui.screens.inventory.InventoryFormScreen
import com.osnordev.abaco.ui.screens.inventory.InventoryListScreen
import com.osnordev.abaco.ui.screens.journal.JournalEntryFormScreen
import com.osnordev.abaco.ui.screens.journal.JournalEntryListScreen
import com.osnordev.abaco.ui.screens.payments.PaymentDueListScreen
import com.osnordev.abaco.ui.screens.qr.QrCodeScreen
import com.osnordev.abaco.ui.screens.reports.ReportsScreen
import com.osnordev.abaco.ui.screens.salary.SalaryScreen
import com.osnordev.abaco.ui.screens.search.SearchScreen
import com.osnordev.abaco.ui.screens.settings.SettingsScreen
import com.osnordev.abaco.ui.screens.taxes.TaxScreen
import com.osnordev.abaco.ui.screens.transactions.TransactionFormScreen
import com.osnordev.abaco.ui.screens.transactions.TransactionListScreen

private const val SLIDE_DURATION_MS = 350

@Composable
fun AppNavHost(
    navController: NavHostController,
    activeModules: Set<AppModule>,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(animationSpec = tween(SLIDE_DURATION_MS)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(SLIDE_DURATION_MS)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(SLIDE_DURATION_MS)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(SLIDE_DURATION_MS)) { it } }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                showCharts = false,
                onNavigateToJournal = { navController.navigate(Screen.Journal.route) { launchSingleTop = true } },
                onNavigateToJournalForm = { navController.navigate("journal_form") }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionListScreen(
                onAddTransaction = { navController.navigate(Screen.TransactionForm.createRoute()) },
                onEditTransaction = { id -> navController.navigate(Screen.TransactionForm.createRoute(id)) }
            )
        }

        composable(
            route = Screen.TransactionForm.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != -1L }
            TransactionFormScreen(transactionId = id, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Taxes.route) { TaxScreen() }

        composable(Screen.Settings.route) { SettingsScreen() }

        composable(Screen.Search.route) { SearchScreen() }

        composable(Screen.Reports.route) { ReportsScreen() }

        composable(Screen.QrCode.route) { QrCodeScreen() }

        // Charts tab (Req 13.1)
        composable(Screen.Charts.route) { ChartsScreen() }

        // Balance sheet
        composable(Screen.BalanceSheet.route) { BalanceSheetScreen() }

        // Payment dues
        composable(Screen.PaymentDues.route) { PaymentDueListScreen() }

        // Budgets
        composable(Screen.Budgets.route) { BudgetScreen() }

        // Salary
        composable(Screen.Salary.route) { SalaryScreen() }

        // Contacts
        composable(Screen.Contacts.route) {
            ContactListScreen(
                onAddContact = { navController.navigate("contact_form") },
                onContactClick = { id -> navController.navigate("contact_form?id=$id") }
            )
        }
        composable(
            route = "contact_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { _ ->
            ContactFormScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Journal (double-entry)
        composable(Screen.Journal.route) {
            JournalEntryListScreen(
                onAddEntry = { navController.navigate("journal_form") }
            )
        }
        composable("journal_form") {
            JournalEntryFormScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Inventory
        composable(Screen.Inventory.route) {
            InventoryListScreen(
                onAddItem = { navController.navigate(Screen.InventoryForm.createRoute()) },
                onItemClick = { id -> navController.navigate(Screen.InventoryForm.createRoute(id)) }
            )
        }
        composable(
            route = Screen.InventoryForm.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != -1L }
            InventoryFormScreen(itemId = id, onNavigateBack = { navController.popBackStack() })
        }
    }
}
