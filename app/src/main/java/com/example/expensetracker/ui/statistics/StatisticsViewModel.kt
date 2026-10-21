package com.example.expensetracker.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.repository.TransactionRepositoryImpl
import com.example.expensetracker.domain.model.TransactionType
import com.example.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*

data class MonthlySummary(
    val monthYear: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val net: Double,
    val categoryBreakdown: Map<String, Double>
)

data class StatisticsUiState(
    val monthlySummaries: List<MonthlySummary> = emptyList()
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepositoryImpl(database.transactionDao())
    }

    val uiState: StateFlow<StatisticsUiState> = repository.getAllTransactions()
        .map { transactions ->
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            
            val groupedByMonth = transactions.groupBy { tx ->
                formatter.format(tx.date)
            }

            val summaries = groupedByMonth.map { (monthYear, txs) ->
                val income = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val net = income - expense
                
                val categoryBreakdown = txs
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryName }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                MonthlySummary(
                    monthYear = monthYear,
                    totalIncome = income,
                    totalExpense = expense,
                    net = net,
                    categoryBreakdown = categoryBreakdown
                )
            }.sortedByDescending { it.monthYear }

            StatisticsUiState(monthlySummaries = summaries)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsUiState()
        )
}
