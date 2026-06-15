package com.example.pocketmanage

import android.app.Application
import com.example.pocketmanage.auth.LocalAuth
import com.example.pocketmanage.data.FirebaseDataStore
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.notifications.BudgetReminderNotifications
import com.example.pocketmanage.notifications.ReminderScheduler

class PocketManageApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalAuth.init(this)
        FirebaseDataStore.init(this)
        FinanceRepository.init(this)
        BudgetReminderNotifications.ensureChannel(this)
        ReminderScheduler.scheduleIfEnabled(this)
    }
}
