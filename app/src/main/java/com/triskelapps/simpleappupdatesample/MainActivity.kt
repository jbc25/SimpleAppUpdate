package com.triskelapps.simpleappupdatesample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkInfo
import com.triskelapps.simpleappupdate.SimpleAppUpdate
import com.triskelapps.simpleappupdate.config.NotificationStyle
import com.triskelapps.simpleappupdate.config.WorkerConfig
import com.triskelapps.simpleappupdatesample.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private lateinit var simpleAppUpdate: SimpleAppUpdate
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        simpleAppUpdate = SimpleAppUpdate(this)

        configureManualCheck()

        // To test bar style.
        checkUpdateBarStyle(false)

        binding.tvAppVersion.text = BuildConfig.VERSION_NAME

        binding.btnStartWorker.setOnClickListener {
            val notificationStyle = NotificationStyle(R.mipmap.simple_app_update_notif_icon, R.color.red)
            val workerConfig =
                WorkerConfig(20, TimeUnit.MINUTES, 10, TimeUnit.MINUTES)

            SimpleAppUpdate.schedulePeriodicChecks(this, BuildConfig.VERSION_CODE, notificationStyle, workerConfig)
            updateWorkerStatus()
        }

        binding.btnCancelWorker.setOnClickListener {
            simpleAppUpdate.cancelWork()
            updateWorkerStatus()
        }

        binding.btnUpdateLogs.setOnClickListener {
            binding.tvLogs.text = simpleAppUpdate.getLogs()
        }

        updateWorkerStatus()

    }

    private fun updateWorkerStatus() {
        val status = simpleAppUpdate.getWorkInfo()?.let { workInfo ->
            workInfo.state.toString() + workInfo.takeIf { it.state == WorkInfo.State.ENQUEUED }.let {
                if (it != null) {
                    getString(R.string.next_check,
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).
                        format(Date(workInfo.nextScheduleTimeMillis))
                    )
                } else {
                    ""
                }
            }
        }
        binding.tvWorkerStatus.text = getString(R.string.status_x, status)

    }


    private fun configureManualCheck() {
        binding.btnCheckForUpdate.setOnClickListener {

            var checkStatusMessage: String? = null

            binding.tvManualCheck.setText(R.string.checking)

            simpleAppUpdate.setUpdateAvailableListener {
                checkStatusMessage = getString(R.string.update_available)
                binding.btnUpdate.visibility = View.VISIBLE
                binding.btnUpdate.setOnClickListener {
                    simpleAppUpdate.launchUpdate()
                }
            }

            simpleAppUpdate.setErrorListener { error ->
                checkStatusMessage = "Error: $error"
            }

            simpleAppUpdate.setFinishListener {
                binding.tvManualCheck.text = checkStatusMessage ?: getString(R.string.no_update_available)
            }

            simpleAppUpdate.checkUpdateAvailable(TAG)
        }
    }


    private fun checkUpdateBarStyle(showBar: Boolean) {
        binding.simpleAppUpdateView.visibility = if (showBar) View.VISIBLE else View.GONE
    }
}