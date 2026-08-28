package com.alecdev.quickcalc.presentation

import android.content.Context

object HistoryRepository {
    private const val PREFS_NAME = "calc_prefs"
    private const val KEY_HISTORY = "history_items"
    private const val ITEM_SEPARATOR = ";;"
    private const val FIELD_SEPARATOR = "|||"
    const val MAX_HISTORY_ITEMS = 30

    fun loadHistory(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val items = raw.split(ITEM_SEPARATOR).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEPARATOR)
            if (parts.size == 2) HistoryItem(parts[0], parts[1]) else null
        }
        return if (items.size > MAX_HISTORY_ITEMS) items.takeLast(MAX_HISTORY_ITEMS) else items
    }

    fun addHistoryEntry(context: Context, expression: String, result: String) {
        if (expression.isBlank() || result.isBlank() || expression == result) return
        val current = loadHistory(context).toMutableList()
        current.add(HistoryItem(expression, result))
        saveHistory(context, current)
    }

    fun saveHistory(context: Context, items: List<HistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cappedItems = if (items.size > MAX_HISTORY_ITEMS) items.takeLast(MAX_HISTORY_ITEMS) else items
        val serialized = cappedItems.joinToString(ITEM_SEPARATOR) { "${it.expression}$FIELD_SEPARATOR${it.result}" }
        prefs.edit().putString(KEY_HISTORY, serialized).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
