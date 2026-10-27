package com.example.expensetracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.CurrencyPreferences
import com.example.expensetracker.data.repository.TransactionRepositoryImpl
import com.example.expensetracker.domain.model.Transaction
import com.example.expensetracker.domain.model.TransactionType
import com.example.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val currencySymbol: String = "$"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val currencyPreferences = CurrencyPreferences(application)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepositoryImpl(database.transactionDao())
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllTransactions(),
        currencyPreferences.currencySymbolFlow
    ) { transactions, currencySymbol ->
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense
        val recent = transactions.take(5)

        HomeUiState(
            transactions = transactions,
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            recentTransactions = recent,
            currencySymbol = currencySymbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            currencyPreferences.saveCurrency(code, symbol)
        }
    }
}
