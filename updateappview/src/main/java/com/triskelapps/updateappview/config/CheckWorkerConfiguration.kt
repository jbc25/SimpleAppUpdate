package com.triskelapps.updateappview.config

import java.util.concurrent.TimeUnit

data class CheckWorkerConfiguration(
    val repeatInterval: Long = 8,
    val repeatIntervalTimeUnit: TimeUnit = TimeUnit.HOURS,
    val flexInterval: Long = 2,
    val flexIntervalTimeUnit: TimeUnit = TimeUnit.HOURS,
)

