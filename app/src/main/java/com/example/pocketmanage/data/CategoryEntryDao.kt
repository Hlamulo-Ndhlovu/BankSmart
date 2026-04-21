package com.example.pocketmanage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryEntryDao {
    @Query("SELECT * FROM category_entries ORDER BY entryDateMillis DESC")
    suspend fun getAll(): List<CategoryEntry>

    @Query("SELECT * FROM category_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntry?

    @Query("SELECT DISTINCT categoryName FROM category_entries ORDER BY categoryName COLLATE NOCASE ASC")
    suspend fun distinctCategoryNames(): List<String>

    @Insert
    suspend fun insert(entry: CategoryEntry): Long

    @Query("DELETE FROM category_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
