package com.example.pocketmanage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(row: FinanceTransaction): Long

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<FinanceTransaction>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    suspend fun getAll(): List<FinanceTransaction>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE amountCents > 0 AND dateMillis >= :start AND dateMillis < :end")
    suspend fun incomeBetween(start: Long, end: Long): Long

    @Query("SELECT COALESCE(SUM(-amountCents), 0) FROM transactions WHERE amountCents < 0 AND dateMillis >= :start AND dateMillis < :end")
    suspend fun expensesBetween(start: Long, end: Long): Long

    @Query("SELECT COUNT(*) FROM transactions WHERE dateMillis >= :start AND dateMillis < :end")
    suspend fun countBetween(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE amountCents < 0 AND dateMillis >= :start AND dateMillis < :end")
    suspend fun expenseCountBetween(start: Long, end: Long): Int

    @Query("SELECT COALESCE(SUM(-amountCents), 0) FROM transactions WHERE category = :category AND amountCents < 0 AND dateMillis >= :start AND dateMillis < :end")
    suspend fun expenseSpentForCategory(category: String, start: Long, end: Long): Long

    @Query("SELECT dateMillis FROM transactions ORDER BY dateMillis DESC")
    suspend fun transactionDatesDesc(): List<Long>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT DISTINCT category FROM transactions ORDER BY category COLLATE NOCASE ASC")
    suspend fun distinctCategories(): List<String>
}
