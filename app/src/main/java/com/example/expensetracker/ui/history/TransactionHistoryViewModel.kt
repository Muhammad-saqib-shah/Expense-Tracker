package com.example.expensetracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.CurrencyPreferences
import com.example.expensetracker.data.repository.TransactionRepositoryImpl
import com.example.expensetracker.domain.model.Transaction
import com.example.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val currencySymbol: String = "$"
)

class TransactionHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val currencyPreferences = CurrencyPreferences(application)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepositoryImpl(database.transactionDao())
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllTransactions(),
        currencyPreferences.currencySymbolFlow
    ) { transactions, currencySymbol ->
        HistoryUiState(transactions = transactions, currencySymbol = currencySymbol)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
