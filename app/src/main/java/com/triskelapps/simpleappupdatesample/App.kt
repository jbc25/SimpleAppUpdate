package com.triskelapps.simpleappupdatesample

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
            WorkerConfig(20, TimeUnit.MINUTES, 10, TimeUnit.MINUTES)

        SimpleAppUpdate.schedulePeriodicChecks(this, BuildConfig.VERSION_CODE, notificationStyle, workerConfig)
    }
}