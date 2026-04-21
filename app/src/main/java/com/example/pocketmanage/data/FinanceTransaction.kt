package com.example.pocketmanage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val amountCents: Long,
    val category: String,
    val note: String,
    val dateMillis: Long,
)
