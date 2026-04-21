package com.example.pocketmanage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    suspend fun getAll(): List<FinanceAccount>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): FinanceAccount?

    @Insert
    suspend fun insert(row: FinanceAccount): Long

    @Update
    suspend fun update(row: FinanceAccount)

    @Query("SELECT COALESCE(SUM(balanceCents), 0) FROM accounts")
    suspend fun totalBalanceCents(): Long

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int
}
