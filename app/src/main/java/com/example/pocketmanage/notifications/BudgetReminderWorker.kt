package com.example.pocketmanage.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BudgetReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (!ReminderPreferences.isEnabled(ctx)) return Result.success()
        return try {
            BudgetReminderNotifications.show(ctx)
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
