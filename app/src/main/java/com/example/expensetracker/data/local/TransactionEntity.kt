package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensetracker.domain.model.Transaction
import com.example.expensetracker.domain.model.TransactionType
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: String,
    val categoryId: String,
    val categoryName: String,
    val timestamp: Long,
    val description: String = ""
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        title = title,
        amount = amount,
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
        categoryId = categoryId,
        categoryName = categoryName,
        date = Date(timestamp),
        description = description
    )

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id,
            title = transaction.title,
            amount = transaction.amount,
            type = transaction.type.name,
            categoryId = transaction.categoryId,
            categoryName = transaction.categoryName,
            timestamp = transaction.date.time,
            description = transaction.description
        )
    }
}
