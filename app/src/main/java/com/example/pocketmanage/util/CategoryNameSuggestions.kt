package com.example.pocketmanage.util

import android.content.Context
import com.example.pocketmanage.R
import com.example.pocketmanage.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object CategoryNameSuggestions {
    private const val PREFS_NAME = "category_prefs"
    private const val KEY_CUSTOM = "custom_labels"

    suspend fun forCategoryEntries(context: Context, db: AppDatabase): List<String> {
        val defaults = context.resources.getStringArray(R.array.category_name_options).toList()
        return mergedWithPrimary(context, db, defaults)
    }

    suspend fun mergedWithPrimary(
        context: Context,
        db: AppDatabase,
        primaryOrdered: List<String>,
    ): List<String> = withContext(Dispatchers.IO) {
        val primaryKeys = primaryOrdered.map { it.lowercase(Locale.getDefault()) }.toSet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getStringSet(KEY_CUSTOM, emptySet()).orEmpty()
        val fromEntries = db.categoryEntryDao().distinctCategoryNames()
        val fromTx = db.transactionDao().distinctCategories()
        val extras = (fromPrefs + fromEntries + fromTx)
            .filter { !primaryKeys.contains(it.lowercase(Locale.getDefault())) }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .sortedBy { it.lowercase(Locale.getDefault()) }
        primaryOrdered + extras
    }

    /**
     * Saves a user-defined label for suggestions. Returns false if blank or already present
     * (case-insensitive), including built-in defaults.
     */
    fun addCustomLabel(context: Context, raw: String): Boolean {
        val name = raw.trim()
        if (name.isEmpty()) return false
        val defaults = context.resources.getStringArray(R.array.category_name_options)
        if (defaults.any { it.equals(name, ignoreCase = true) }) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_CUSTOM, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (set.any { it.equals(name, ignoreCase = true) }) return false
        set.add(name)
        prefs.edit().putStringSet(KEY_CUSTOM, set).apply()
        return true
    }

    fun rememberUsedLabel(context: Context, raw: String) {
        val name = raw.trim()
        if (name.isEmpty()) return
        addCustomLabel(context, name)
    }
}
