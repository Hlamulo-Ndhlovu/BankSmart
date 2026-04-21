package com.example.pocketmanage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BudgetCategoryDao {
    @Query("SELECT * FROM budget_categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getAll(): List<BudgetCategoryEntity>

    @Insert
    suspend fun insert(row: BudgetCategoryEntity): Long

    @Query("SELECT COUNT(*) FROM budget_categories")
    suspend fun count(): Int
}
