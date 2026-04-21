package com.example.pocketmanage.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

object MoneyFormat {
    private val locale = Locale("en", "ZA")

    fun formatCents(cents: Long): String {
        val fmt = NumberFormat.getCurrencyInstance(locale)
        return fmt.format(cents / 100.0)
    }

    fun formatCentsUnsigned(cents: Long): String {
        val fmt = NumberFormat.getCurrencyInstance(locale)
        return fmt.format(kotlin.math.abs(cents) / 100.0)
    }

    /** Plain decimal string for editing (e.g. "1234.56"). */
    fun centsToDecimalString(cents: Long): String =
        String.format(Locale.US, "%.2f", cents / 100.0)

    fun parseRandToCents(text: String): Long? {
        val t = text.trim().replace(",", ".").replace(Regex("\\s"), "")
        if (t.isEmpty() || t == "." || t == "-" || t == "-.") return null
        return try {
            val v = t.toDouble()
            (v * 100.0).roundToLong()
        } catch (_: NumberFormatException) {
            null
        }
    }
}
