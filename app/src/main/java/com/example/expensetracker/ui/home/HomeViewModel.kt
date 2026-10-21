package com.example.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.repository.TransactionRepositoryImpl
import com.example.expensetracker.domain.model.Transaction
import com.example.expensetracker.domain.model.TransactionType
import com.example.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepositoryImpl(database.transactionDao())
    }

    val uiState: StateFlow<HomeUiState> = repository.getAllTransactions()
        .map { transactions ->
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val totalBalance = totalIncome - totalExpense
            val recent = transactions.take(5)

            HomeUiState(
                transactions = transactions,
                totalBalance = totalBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                recentTransactions = recent
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )
}
