package com.example.pocketmanage.data

import android.content.Context
import androidx.room.withTransaction
import com.example.pocketmanage.util.CategoryNameSuggestions
import com.example.pocketmanage.util.MoneyFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs

object FinanceRepository {
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.get(context.applicationContext)
    }

    private fun db(): AppDatabase = db

    fun currentMonthBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    suspend fun ensureSeeded() {
        FinanceBootstrap.seedIfEmpty(db())
    }

    suspend fun totalBalanceFormatted(): String =
        withContext(Dispatchers.IO) {
            MoneyFormat.formatCents(db().accountDao().totalBalanceCents())
        }

    suspend fun monthIncomeExpense(): Pair<String, String> =
        withContext(Dispatchers.IO) {
            val (start, end) = currentMonthBounds()
            val inc = db().transactionDao().incomeBetween(start, end)
            val exp = db().transactionDao().expensesBetween(start, end)
            MoneyFormat.formatCents(inc) to MoneyFormat.formatCents(exp)
        }

    suspend fun monthIncomeExpenseCents(): Pair<Long, Long> =
        withContext(Dispatchers.IO) {
            val (start, end) = currentMonthBounds()
            db().transactionDao().incomeBetween(start, end) to
                db().transactionDao().expensesBetween(start, end)
        }

    data class BudgetRowUi(
        val name: String,
        val spentCents: Long,
        val limitCents: Long,
        val colorIndex: Int,
    )

    suspend fun dashboardBudgetRows(): List<BudgetRowUi> =
        withContext(Dispatchers.IO) {
            val (start, end) = currentMonthBounds()
            val cats = db().budgetCategoryDao().getAll()
            cats.mapIndexed { index, cat ->
                val spent = db().transactionDao().expenseSpentForCategory(cat.name, start, end)
                BudgetRowUi(cat.name, spent, cat.budgetLimitCents, index % 4)
            }
        }

    suspend fun recentTransactions(limit: Int = 20): List<FinanceTransaction> =
        withContext(Dispatchers.IO) {
            db().transactionDao().recent(limit)
        }

    suspend fun accounts(): List<FinanceAccount> =
        withContext(Dispatchers.IO) {
            db().accountDao().getAll()
        }

    suspend fun accountsTotalFormatted(): String =
        withContext(Dispatchers.IO) {
            MoneyFormat.formatCents(db().accountDao().totalBalanceCents())
        }

    suspend fun addAccount(name: String, balanceCents: Long = 0L) =
        withContext(Dispatchers.IO) {
            db().accountDao().insert(
                FinanceAccount(name = name.trim(), balanceCents = balanceCents, iconKey = "custom"),
            )
        }

    suspend fun updateAccountBalance(accountId: Long, balanceCents: Long) =
        withContext(Dispatchers.IO) {
            val acc = db().accountDao().getById(accountId) ?: return@withContext
            db().accountDao().update(acc.copy(balanceCents = balanceCents))
        }

    suspend fun addTransaction(
        accountId: Long,
        amountCents: Long,
        category: String,
        note: String,
        isExpense: Boolean,
    ) = withContext(Dispatchers.IO) {
        val signed = if (isExpense) -abs(amountCents) else abs(amountCents)
        val now = System.currentTimeMillis()
        db().withTransaction {
            db().transactionDao().insert(
                FinanceTransaction(
                    accountId = accountId,
                    amountCents = signed,
                    category = category.trim(),
                    note = note.trim(),
                    dateMillis = now,
                ),
            )
            val acc = db().accountDao().getById(accountId)
                ?: throw IllegalStateException("Account not found")
            db().accountDao().update(acc.copy(balanceCents = acc.balanceCents + signed))
        }
    }

    suspend fun budgetCategoryNames(context: Context): List<String> =
        withContext(Dispatchers.IO) {
            val budget = db().budgetCategoryDao().getAll().map { it.name }
            CategoryNameSuggestions.mergedWithPrimary(context, db(), budget)
        }

    suspend fun transactionCount(): Int =
        withContext(Dispatchers.IO) {
            db().transactionDao().count()
        }

    suspend fun budgetCategoriesCount(): Int =
        withContext(Dispatchers.IO) {
            db().budgetCategoryDao().count()
        }
}
