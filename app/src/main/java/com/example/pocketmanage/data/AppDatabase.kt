package com.example.pocketmanage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CategoryEntry::class,
        FinanceAccount::class,
        FinanceTransaction::class,
        BudgetCategoryEntity::class,
        UserEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryEntryDao(): CategoryEntryDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetCategoryDao(): BudgetCategoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocketmanage.db",
                )
                    .addMigrations(DbMigrations.MIGRATION_1_2, DbMigrations.MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
