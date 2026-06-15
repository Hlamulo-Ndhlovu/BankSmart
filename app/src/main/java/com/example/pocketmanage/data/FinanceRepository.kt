package com.example.pocketmanage.data

import android.content.Context
import androidx.room.withTransaction
import com.example.pocketmanage.util.CategoryNameSuggestions
import com.example.pocketmanage.util.MoneyFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

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
        syncCloudSnapshot()
    }

    private suspend fun syncCloudSnapshot() {
        if (!FirebaseDataStore.isEnabled()) return
        val accounts = db().accountDao().getAll()
        val budgets = db().budgetCategoryDao().getAll()
        val transactions = db().transactionDao().getAll()
        FirebaseDataStore.syncFinanceSnapshot(accounts, budgets, transactions)
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

    data class RewardsUi(
        val points: Int,
        val level: Int,
        val badgeName: String,
        val levelProgressPercent: Int,
        val pointsToNextLevel: Int,
        val streakDays: Int,
        val savingsCurrentCents: Long,
        val savingsGoalCents: Long,
        val savingsProgressPercent: Int,
        val transactionChallengeProgress: Int,
        val transactionChallengeGoal: Int,
        val budgetsWithinLimit: Int,
        val budgetCount: Int,
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

    suspend fun rewardsStatus(): RewardsUi =
        withContext(Dispatchers.IO) {
            val (monthStart, monthEnd) = currentMonthBounds()
            val expenseCount = db().transactionDao().expenseCountBetween(monthStart, monthEnd)
            val rows = dashboardBudgetRows()
            val budgetsWithinLimit = rows.count { row ->
                row.limitCents > 0 && row.spentCents <= row.limitCents
            }
            val transactionDates = db().transactionDao().transactionDatesDesc()
            val streak = spendingStreakDays(transactionDates)
            val trackedDaysThisMonth = transactionDates
                .filter { it >= monthStart && it < monthEnd }
                .map { startOfDayMillis(it) }
                .toSet()
                .size
            val savingsCurrent = db().accountDao().getAll()
                .firstOrNull { account ->
                    account.name.equals("Savings", ignoreCase = true) ||
                        account.iconKey.equals("savings", ignoreCase = true)
                }
                ?.balanceCents
                ?.coerceAtLeast(0)
                ?: 0L
            val savingsGoal = 50_000L
            val savingsProgress = if (savingsGoal > 0) {
                min(100, ((savingsCurrent * 100f) / savingsGoal).roundToInt())
            } else {
                0
            }
            val points = expenseCount * 10 + budgetsWithinLimit * 20 + streak * 5
            val level = points / POINTS_PER_LEVEL
            val pointsIntoLevel = points % POINTS_PER_LEVEL
            RewardsUi(
                points = points,
                level = level,
                badgeName = badgeNameForLevel(level),
                levelProgressPercent = ((pointsIntoLevel * 100f) / POINTS_PER_LEVEL).roundToInt(),
                pointsToNextLevel = POINTS_PER_LEVEL - pointsIntoLevel,
                streakDays = streak,
                savingsCurrentCents = savingsCurrent,
                savingsGoalCents = savingsGoal,
                savingsProgressPercent = savingsProgress,
                transactionChallengeProgress = min(trackedDaysThisMonth, 7),
                transactionChallengeGoal = 7,
                budgetsWithinLimit = budgetsWithinLimit,
                budgetCount = rows.size,
            )
        }

    private const val POINTS_PER_LEVEL = 100

    private fun badgeNameForLevel(level: Int): String =
        when {
            level >= 10 -> "Diamond Badge"
            level >= 7 -> "Platinum Badge"
            level >= 5 -> "Gold Badge"
            level >= 3 -> "Silver Badge"
            level >= 1 -> "Bronze Badge"
            else -> "Starter Badge"
        }

    private fun spendingStreakDays(dateMillis: List<Long>): Int {
        val daysWithTransactions = dateMillis
            .map { startOfDayMillis(it) }
            .toSet()
        if (daysWithTransactions.isEmpty()) return 0

        val cal = Calendar.getInstance()
        var expectedDay = startOfDayMillis(cal.timeInMillis)
        if (expectedDay !in daysWithTransactions) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            expectedDay = startOfDayMillis(cal.timeInMillis)
        }

        var streak = 0
        while (expectedDay in daysWithTransactions) {
            streak += 1
            cal.timeInMillis = expectedDay
            cal.add(Calendar.DAY_OF_YEAR, -1)
            expectedDay = startOfDayMillis(cal.timeInMillis)
        }
        return streak
    }

    private fun startOfDayMillis(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
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
            val account = FinanceAccount(name = name.trim(), balanceCents = balanceCents, iconKey = "custom")
            val id = db().accountDao().insert(account)
            FirebaseDataStore.syncAccount(
                account.copy(id = id),
            )
        }

    suspend fun updateAccountBalance(accountId: Long, balanceCents: Long) =
        withContext(Dispatchers.IO) {
            val acc = db().accountDao().getById(accountId) ?: return@withContext
            val updated = acc.copy(balanceCents = balanceCents)
            db().accountDao().update(updated)
            FirebaseDataStore.syncAccount(updated)
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
        var savedTransaction: FinanceTransaction? = null
        var updatedAccount: FinanceAccount? = null
        db().withTransaction {
            val transaction = FinanceTransaction(
                accountId = accountId,
                amountCents = signed,
                category = category.trim(),
                note = note.trim(),
                dateMillis = now,
            )
            val transactionId = db().transactionDao().insert(transaction)
            savedTransaction = transaction.copy(id = transactionId)
            val acc = db().accountDao().getById(accountId)
                ?: throw IllegalStateException("Account not found")
            updatedAccount = acc.copy(balanceCents = acc.balanceCents + signed)
            db().accountDao().update(updatedAccount!!)
        }
        savedTransaction?.let(FirebaseDataStore::syncTransaction)
        updatedAccount?.let(FirebaseDataStore::syncAccount)
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
