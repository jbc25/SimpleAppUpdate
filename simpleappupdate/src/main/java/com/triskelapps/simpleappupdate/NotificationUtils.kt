package com.triskelapps.simpleappupdate

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat


class NotificationUtils(
    private val context: Context,
    private val notificationIcon: Int,
    private val notificationColor: Int
) {

    private val TAG: String = NotificationUtils::class.java.simpleName
    private val channelId = "channel_update_app"
    private val notificationId = 260724

    fun showNotification(packageName: String) {

        initializeOreoChannelsNotification()
        getBaseBuilder(context, packageName)?.run {
            setContentTitle(context.getString(R.string.update_available))
            setContentText(context.getString(R.string.press_to_update))

            val notification = build()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
            saveLog(context, "Notification sent", TAG)
        }

    }

    private fun getBaseBuilder(context: Context, packageName: String): NotificationCompat.Builder? {

        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            val resolveinfo_list: List<ResolveInfo> =
                context.packageManager.queryIntentActivities(intent, 0)

            for (info in resolveinfo_list) {
                if (info.activityInfo.packageName.equals(packageName, ignoreCase = true)) {

                    intent.setComponent(ComponentName(info.activityInfo.packageName, info.activityInfo.name))
                    break
                }
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notifBuilder =
                NotificationCompat.Builder(context, channelId).apply {
                    setSound(defaultSoundUri)
                    setSmallIcon(notificationIcon)
                    setAutoCancel(true)
                    setContentIntent(pendingIntent)

                    notificationColor.takeIf { it != -1 }?.let { setColor(it) }
                }

            return notifBuilder
        } catch (e: ActivityNotFoundException) {
        }
        return null
    }

    private fun initializeOreoChannelsNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(
                channelId,
                R.string.channel_notif_update_app_name,
                R.string.channel_notif_update_app_description
            )
        }
    }

    @SuppressLint("NewApi")
    private fun createChannel(
        channelId: String,
        nameStringId: Int,
        descriptionStringId: Int
    ) {
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val name: CharSequence = context.getString(nameStringId)
            val description = context.getString(descriptionStringId)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }
    }


}