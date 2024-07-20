package com.triskelapps.simpleappupdate.config

import java.util.concurrent.TimeUnit

data class WorkerConfig @JvmOverloads constructor (
    val repeatInterval: Long = 8,
    val repeatIntervalTimeUnit: TimeUnit = TimeUnit.HOURS,
    val flexInterval: Long = 2,
    val flexIntervalTimeUnit: TimeUnit = TimeUnit.HOURS,
)

