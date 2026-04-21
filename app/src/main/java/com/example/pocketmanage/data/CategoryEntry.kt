package com.example.pocketmanage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_entries")
data class CategoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryDateMillis: Long,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val description: String,
    val categoryName: String,
    val photoPath: String?,
)
