package com.example.expensetracker.domain.model

import java.util.Date

data class Transaction(
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String,
    val date: Date,
    val description: String = ""
)
