package com.example.expensetracker.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val type: TransactionType
) {
    companion object {
        val defaultCategories = listOf(
            Category("food", "Food & Dining", "restaurant", TransactionType.EXPENSE),
            Category("rent", "Rent & Housing", "home", TransactionType.EXPENSE),
            Category("utilities", "Utilities", "bolt", TransactionType.EXPENSE),
            Category("transport", "Transportation", "directions_car", TransactionType.EXPENSE),
            Category("entertainment", "Entertainment", "movie", TransactionType.EXPENSE),
            Category("salary", "Salary", "work", TransactionType.INCOME),
            Category("freelance", "Freelance", "laptop", TransactionType.INCOME),
            Category("investment", "Investment", "trending_up", TransactionType.INCOME),
            Category("other_expense", "Other Expense", "category", TransactionType.EXPENSE),
            Category("other_income", "Other Income", "attach_money", TransactionType.INCOME)
        )
    }
}
