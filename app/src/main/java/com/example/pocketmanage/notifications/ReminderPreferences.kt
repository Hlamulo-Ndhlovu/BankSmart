package com.example.pocketmanage.notifications

import android.content.Context
import java.util.Calendar

object ReminderPreferences {
    private const val PREFS = "budget_reminders"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun getHour(context: Context): Int = prefs(context).getInt(KEY_HOUR, 9)

    fun getMinute(context: Context): Int = prefs(context).getInt(KEY_MINUTE, 0)

    fun setTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
    }

    /** Milliseconds until the next occurrence of the chosen clock time (today or tomorrow). */
    fun nextDelayMillis(context: Context): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, getHour(context))
        cal.set(Calendar.MINUTE, getMinute(context))
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return (cal.timeInMillis - System.currentTimeMillis()).coerceAtLeast(60_000L)
    }
}
