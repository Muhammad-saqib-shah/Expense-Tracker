package com.example.expensetracker.ui.addedit

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: AddEditTransactionViewModel = viewModel(
        factory = AddEditTransactionViewModelFactory(context, transactionId)
    )

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.date.time
    )

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AddEditTransactionViewModel.UiEvent.SaveSuccess -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId == null) "Add Transaction" else "Edit Transaction") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveTransaction() }) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector (Income / Expense)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = viewModel.type == TransactionType.EXPENSE,
                    onClick = { viewModel.onTypeChanged(TransactionType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Expense")
                }
                SegmentedButton(
                    selected = viewModel.type == TransactionType.INCOME,
                    onClick = { viewModel.onTypeChanged(TransactionType.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Income")
                }
            }

            // Title
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title / Merchant") },
                isError = viewModel.titleError != null,
                supportingText = {
                    val error = viewModel.titleError
                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount
            OutlinedTextField(
                value = viewModel.amount,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = viewModel.amountError != null,
                supportingText = {
                    val error = viewModel.amountError
                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category Selection Header
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Category Chips
            val filteredCategories = Category.defaultCategories.filter { it.type == viewModel.type }
            ExperimentalFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp
            ) {
                filteredCategories.forEach { category ->
                    FilterChip(
                        selected = viewModel.selectedCategory?.id == category.id,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.name) }
                    )
                }
            }
            val catError = viewModel.categoryError
            if (catError != null) {
                Text(
                    text = catError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Date Selection
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Date")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Date: ${dateFormat.format(viewModel.date)}")
            }

            // Description
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChanged(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ExperimentalFlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        var width = 0
        var height = 0
        var currentRowWidth = 0
        var currentRowHeight = 0
        val rowHeights = mutableListOf<Int>()
        val rowWidths = mutableListOf<Int>()
        val rowPlaceableCounts = mutableListOf<Int>()

        var count = 0
        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth && count > 0) {
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                rowPlaceableCounts.add(count)
                width = maxOf(width, currentRowWidth)
                height += currentRowHeight + verticalSpacing.roundToPx()
                currentRowWidth = placeable.width + horizontalSpacing.roundToPx()
                currentRowHeight = placeable.height
                count = 1
            } else {
                currentRowWidth += placeable.width + if (count > 0) horizontalSpacing.roundToPx() else 0
                currentRowHeight = maxOf(currentRowHeight, placeable.height)
                count++
            }
        }
        if (count > 0) {
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
            rowPlaceableCounts.add(count)
            width = maxOf(width, currentRowWidth)
            height += currentRowHeight
        }

        layout(width = maxOf(width, constraints.minWidth), height = maxOf(height, constraints.minHeight)) {
            var y = 0
            var placeableIndex = 0
            rowHeights.forEachIndexed { rowIndex, rowHeight ->
                var x = 0
                val placeableCount = rowPlaceableCounts[rowIndex]
                for (i in 0 until placeableCount) {
                    val placeable = placeables[placeableIndex++]
                    placeable.placeRelative(x, y + (rowHeight - placeable.height) / 2)
                    x += placeable.width + horizontalSpacing.roundToPx()
                }
                y += rowHeight + verticalSpacing.roundToPx()
            }
        }
    }
}
