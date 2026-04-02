package com.osnordev.abaco.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Transactions : Screen("transactions")
    data object TransactionForm : Screen("transaction_form?id={id}") {
        fun createRoute(id: Long? = null) =
            if (id != null) "transaction_form?id=$id" else "transaction_form"
    }
    data object Taxes : Screen("taxes")
    data object Settings : Screen("settings")
    data object Search : Screen("search")
    data object Reports : Screen("reports")
    data object QrCode : Screen("qr_code")
    data object Charts : Screen("charts")
    data object BalanceSheet : Screen("balance_sheet")
    data object PaymentDues : Screen("payment_dues")
    data object Budgets : Screen("budgets")
    data object Contacts : Screen("contacts")
    data object Journal : Screen("journal")
    data object Salary : Screen("salary")
    data object Inventory : Screen("inventory")
    data object InventoryForm : Screen("inventory_form?id={id}") {
        fun createRoute(id: Long? = null) =
            if (id != null) "inventory_form?id=$id" else "inventory_form"
    }
}
