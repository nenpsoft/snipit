package com.nenpsoft.snipit.common

import android.content.Context
import androidx.preference.PreferenceManager

private const val Color = "color"

fun saveDefaultColor(color: Int, context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Color, color).apply()
}

fun loadDefaultColor(context: Context): Int {
    return PreferenceManager.getDefaultSharedPreferences(context).getInt(Color, 0)
}