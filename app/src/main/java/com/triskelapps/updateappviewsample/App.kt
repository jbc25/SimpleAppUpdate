package com.triskelapps.updateappviewsample

import android.app.Application
import com.triskelapps.simpleappupdate.SimpleAppUpdate
import com.triskelapps.simpleappupdate.config.NotificationStyle
import com.triskelapps.simpleappupdate.config.WorkerConfig
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val notificationStyle = NotificationStyle(R.mipmap.simple_app_update_notif_icon, R.color.red)
        val workerConfig =
            WorkerConfig(30, TimeUnit.MINUTES, 15, TimeUnit.MINUTES)

        SimpleAppUpdate.schedulePeriodicChecks(this, BuildConfig.VERSION_CODE, notificationStyle, workerConfig)
    }
}