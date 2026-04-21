package com.example.pocketmanage.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val UNIQUE_WORK_NAME = "budget_daily_reminder"

    fun schedule(context: Context) {
        val app = context.applicationContext
        val delayMs = ReminderPreferences.nextDelayMillis(app)
        val work = PeriodicWorkRequestBuilder<BudgetReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            work,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    fun scheduleIfEnabled(context: Context) {
        if (ReminderPreferences.isEnabled(context.applicationContext)) {
            schedule(context)
        }
    }
}
