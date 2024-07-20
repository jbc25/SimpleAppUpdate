package com.triskelapps.simpleappupdate

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.common.util.concurrent.ListenableFuture
import com.triskelapps.simpleappupdate.config.NotificationStyle
import com.triskelapps.simpleappupdate.config.WorkerConfig
import java.util.concurrent.ExecutionException

class SimpleAppUpdate(private val context: Context) {


    private val TEMPLATE_URL_GOOGLE_PLAY_APP_HTTP: String =
        "https://play.google.com/store/apps/details?id=%s"
    private val TEMPLATE_URL_GOOGLE_PLAY_APP_DIRECT: String = "market://details?id=%s"

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var appUpdateInfo: AppUpdateInfo? = null
    private var onUpdateAvailable: () -> Unit = {}
    private var onCheckUpdateError: (String) -> Unit = {}
    private var onCheckUpdateFinish: () -> Unit = {}

    companion object {

        private val TAG: String = SimpleAppUpdate::class.java.simpleName

        const val UNIQUE_WORK_NAME = "SimpleAppUpdateCheckWork"

        @JvmStatic
        @JvmOverloads
        fun schedulePeriodicChecks(
            context: Context,
            versionCode: Int,
            notificationStyle: NotificationStyle,
            workerConfig: WorkerConfig = WorkerConfig(),
        ) {

            saveLog(context, "Scheduling periodic checks - $workerConfig", TAG)

            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = Data.Builder()
                .putInt(CheckAppUpdateWorker.VERSION_CODE, versionCode)
                .putInt(CheckAppUpdateWorker.NOTIFICATION_ICON, notificationStyle.notificationIcon)
                .putInt(CheckAppUpdateWorker.NOTIFICATION_COLOR, notificationStyle.notificationColor ?: -1)
                .build()

            val updateAppCheckWork = PeriodicWorkRequestBuilder<CheckAppUpdateWorker>(
                workerConfig.repeatInterval, workerConfig.repeatIntervalTimeUnit,
                workerConfig.flexInterval, workerConfig.flexIntervalTimeUnit,)
                .setConstraints(constraints)
                .setInputData(data)
                .build()


            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                "${context.packageName}.$UNIQUE_WORK_NAME",
                ExistingPeriodicWorkPolicy.UPDATE, updateAppCheckWork
            )


        }

    }

    fun setUpdateAvailableListener(updateAvailableListener: () -> Unit = {}) {
        this.onUpdateAvailable = updateAvailableListener
    }

    fun setFinishListener(updateFinishListener: () -> Unit = {}) {
        this.onCheckUpdateFinish = updateFinishListener
    }

    fun setErrorListener(onCheckUpdateError: (String) -> Unit = {}) {
        this.onCheckUpdateError = onCheckUpdateError
    }

    @JvmOverloads
    fun checkUpdateAvailable(logTag: String = TAG) {

        saveLog(context, "Checking update...", logTag)

        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask
            .addOnCompleteListener { task: Task<AppUpdateInfo?> ->
                if (task.isSuccessful) {
                    appUpdateInfo = task.result
                    if (appUpdateInfo!!.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        onUpdateAvailable()
                        saveLog(context, "onUpdateAvailable", logTag)
                    }
                } else {
                    onCheckUpdateError(task.exception.toString())
                    Log.e(TAG, "checkUpdateAvailable: ", task.exception)
                    saveLog(context, "onCheckUpdateError: ${task.exception}", logTag)
                }

                onCheckUpdateFinish()
                saveLog(context, "onFinish", logTag)

            }
    }

    fun launchUpdate() {
        if (appUpdateInfo != null) {
            if (appUpdateInfo!!.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                val options: AppUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true).build()

                if (context is Activity) {
                    appUpdateManager.startUpdateFlow(appUpdateInfo!!, context, options)
                } else {
                    throw java.lang.IllegalStateException("context is not an Activity")
                }
            } else {
                openGooglePlay()
            }
        }

    }

    private fun openGooglePlay() {
        val httpUrl = String.format(TEMPLATE_URL_GOOGLE_PLAY_APP_HTTP, context.packageName)
        val directUrl = String.format(TEMPLATE_URL_GOOGLE_PLAY_APP_DIRECT, context.packageName)

        val directPlayIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directUrl))
        try {
            context.startActivity(directPlayIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl)))
        }
    }

    fun onResume() {
        if (appUpdateInfo != null && appUpdateInfo!!.updateAvailability()
            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            val options: AppUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                .setAllowAssetPackDeletion(true).build()

            if (context is Activity) {
                appUpdateManager.startUpdateFlow(appUpdateInfo!!, context, options)
            } else {
                throw java.lang.IllegalStateException("context is not an Activity")
            }
        }
    }


    fun getWorkStatus(): WorkInfo.State? {
        val instance: WorkManager = WorkManager.getInstance(context)
        val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosForUniqueWork(
            "${context.packageName}.$UNIQUE_WORK_NAME"
        )

        try {
            val workInfoList: List<WorkInfo> = statuses.get()
            Log.i(TAG, "getWorkerStatus: workInfoList count: ${workInfoList.size}")
            for (workInfo in workInfoList) {
                Log.i(TAG, "getWorkerStatus: workInfo: $workInfo")
                return workInfo.state
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return null
    }

    fun cancelWork() {
        WorkManager.getInstance(context)
            .cancelUniqueWork("${context.packageName}.$UNIQUE_WORK_NAME")
    }

    fun getLogs() = getLogs(context)


}
