package com.example.pocketmanage.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DbMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `users` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`email` TEXT NOT NULL, " +
                    "`displayName` TEXT NOT NULL, " +
                    "`passwordHashB64` TEXT NOT NULL, " +
                    "`saltB64` TEXT NOT NULL)",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `accounts` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`balanceCents` INTEGER NOT NULL, " +
                    "`iconKey` TEXT NOT NULL DEFAULT '')",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transactions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`accountId` INTEGER NOT NULL, " +
                    "`amountCents` INTEGER NOT NULL, " +
                    "`category` TEXT NOT NULL, " +
                    "`note` TEXT NOT NULL DEFAULT '', " +
                    "`dateMillis` INTEGER NOT NULL)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `budget_categories` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`budgetLimitCents` INTEGER NOT NULL, " +
                    "`sortOrder` INTEGER NOT NULL DEFAULT 0)",
            )
        }
    }
}
