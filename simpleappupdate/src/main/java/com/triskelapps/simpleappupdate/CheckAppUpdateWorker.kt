package com.triskelapps.simpleappupdate

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("RestrictedApi")
class CheckAppUpdateWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val TAG: String = CheckAppUpdateWorker::class.java.simpleName
    private val PREF_LAST_VERSION_NOTIFIED = "pref_last_version_notified"

    private var notificationIcon: Int = -1
    private var notificationColor: Int = -1

    val packageName: String = context.packageName

    private val simpleAppUpdate = SimpleAppUpdate(context)
    private lateinit var prefs: SharedPreferences

    companion object {
        val VERSION_CODE = "version_code"
        val NOTIFICATION_ICON = "notification_icon"
        val NOTIFICATION_COLOR = "notification_color"
    }

    override suspend fun doWork(): Result {

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val lastVersionNotified = prefs.getInt(PREF_LAST_VERSION_NOTIFIED, 0)

        val versionCode = inputData.getInt(VERSION_CODE, -1)
        notificationIcon = inputData.getInt(NOTIFICATION_ICON, -1)
        notificationColor = inputData.getInt(NOTIFICATION_COLOR, -1)

        saveLog(applicationContext, "Worker started. lastVersionNotified: $lastVersionNotified, version code: $versionCode", TAG)

        return if (lastVersionNotified < versionCode) {
            checkAppUpdateAvailable()
        } else {
            Result.success()
        }
    }


    private suspend fun checkAppUpdateAvailable(): Result = suspendCoroutine { continuation ->
        simpleAppUpdate.setUpdateAvailableListener {
            prepareAndShowNotification()
        }
        simpleAppUpdate.setFinishListener {
            continuation.resume(Result.success())
        }
        simpleAppUpdate.checkUpdateAvailable(TAG)
    }

    private fun prepareAndShowNotification() {

        NotificationUtils(applicationContext, notificationIcon, notificationColor).showNotification(packageName)

        val versionCode = inputData.getInt(VERSION_CODE, -1)
        prefs.edit().putInt(PREF_LAST_VERSION_NOTIFIED, versionCode).apply()
    }

}
