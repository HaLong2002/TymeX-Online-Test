package com.example.currency_converted

import android.content.Context

// Create a preferences helper to track first launch
class PreferencesHelper(context: Context) {
    private val prefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean("is_first_launch", false).apply()
    }
}