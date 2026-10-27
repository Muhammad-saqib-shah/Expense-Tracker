package com.example.expensetracker.ui.addedit

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.CurrencyPreferences
import com.example.expensetracker.data.repository.TransactionRepositoryImpl
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.Transaction
import com.example.expensetracker.domain.model.TransactionType
import com.example.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class AddEditTransactionViewModel(
    application: Application,
    private val transactionId: Long?
) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val currencyPreferences = CurrencyPreferences(application)

    var currencySymbol by mutableStateOf("$")
        private set

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepositoryImpl(database.transactionDao())
        if (transactionId != null && transactionId != -1L) {
            loadTransaction(transactionId)
        }
        viewModelScope.launch {
            currencyPreferences.currencySymbolFlow.collectLatest { symbol ->
                currencySymbol = symbol
            }
        }
    }

    var title by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var type by mutableStateOf(TransactionType.EXPENSE)
        private set
    var selectedCategory by mutableStateOf<Category?>(null)
        private set
    var date by mutableStateOf(Date())
        private set
    var description by mutableStateOf("")
        private set

    var titleError by mutableStateOf<String?>(null)
        private set
    var amountError by mutableStateOf<String?>(null)
        private set
    var categoryError by mutableStateOf<String?>(null)
        private set

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun onTitleChanged(newTitle: String) {
        title = newTitle
        if (newTitle.isNotBlank()) titleError = null
    }

    fun onAmountChanged(newAmount: String) {
        amount = newAmount
        if (newAmount.toDoubleOrNull() != null && newAmount.toDouble() > 0.0) {
            amountError = null
        }
    }

    fun onTypeChanged(newType: TransactionType) {
        type = newType
        if (selectedCategory?.type != newType) {
            selectedCategory = null
        }
    }

    fun onCategorySelected(category: Category) {
        selectedCategory = category
        categoryError = null
    }

    fun onDateChanged(newDate: Date) {
        date = newDate
    }

    fun onDescriptionChanged(newDescription: String) {
        description = newDescription
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { tx ->
                title = tx.title
                amount = tx.amount.toString()
                type = tx.type
                selectedCategory = Category.defaultCategories.find { it.id == tx.categoryId }
                    ?: Category(tx.categoryId, tx.categoryName, "category", tx.type)
                date = tx.date
                description = tx.description
            }
        }
    }

    fun saveTransaction() {
        var hasError = false
        if (title.isBlank()) {
            titleError = "Title cannot be empty"
            hasError = true
        }
        val parsedAmount = amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0.0) {
            amountError = "Enter a valid amount greater than 0"
            hasError = true
        }
        if (selectedCategory == null) {
            categoryError = "Please select a category"
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            val transaction = Transaction(
                id = transactionId ?: 0L,
                title = title.trim(),
                amount = parsedAmount!!,
                type = type,
                categoryId = selectedCategory!!.id,
                categoryName = selectedCategory!!.name,
                date = date,
                description = description.trim()
            )

            if (transactionId == null || transactionId == -1L) {
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
            _uiEvent.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        object SaveSuccess : UiEvent()
    }
}

class AddEditTransactionViewModelFactory(
    private val application: Application,
    private val transactionId: Long?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditTransactionViewModel::class.java)) {
            return AddEditTransactionViewModel(application, transactionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
