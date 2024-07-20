package com.triskelapps.simpleappupdate

import android.content.Context
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun saveLog(context: Context, text: String, tag: String) {

    val logs = getLogs(context)
    val logArray = logs.split("\n").toMutableList()

    val datetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    logArray.add("$datetime - $tag - $text")

    if (logArray.size > 100) {
        logArray.removeAt(0)
    }

    val newLogs = logArray.joinToString("\n")

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    prefs.edit().putString("logs", newLogs).apply()

}

fun getLogs(context: Context): String {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val logs = prefs.getString("logs", null) ?: ""
    return logs
}