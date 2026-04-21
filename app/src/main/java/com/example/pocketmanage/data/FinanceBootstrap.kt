package com.example.pocketmanage.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FinanceBootstrap {
    suspend fun seedIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val accounts = db.accountDao()
        val budgets = db.budgetCategoryDao()
        if (accounts.count() == 0) {
            listOf(
                FinanceAccount(name = "Card", balanceCents = 0, iconKey = "card"),
                FinanceAccount(name = "Cash", balanceCents = 0, iconKey = "cash"),
                FinanceAccount(name = "Pay Pal", balanceCents = 0, iconKey = "paypal"),
                FinanceAccount(name = "Savings", balanceCents = 0, iconKey = "savings"),
            ).forEach { accounts.insert(it) }
        }
        if (budgets.count() == 0) {
            listOf(
                BudgetCategoryEntity(name = "Food", budgetLimitCents = 50_000, sortOrder = 0),
                BudgetCategoryEntity(name = "Transport", budgetLimitCents = 20_000, sortOrder = 1),
                BudgetCategoryEntity(name = "Shopping", budgetLimitCents = 100_000, sortOrder = 2),
                BudgetCategoryEntity(name = "Bills", budgetLimitCents = 120_000, sortOrder = 3),
            ).forEach { budgets.insert(it) }
        }
    }
}
