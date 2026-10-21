package com.example.expensetracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object AddEditTransaction : Screen("add_edit_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null): String {
            return if (transactionId != null) {
                "add_edit_transaction?transactionId=$transactionId"
            } else {
                "add_edit_transaction"
            }
        }
    }
}
