package com.triskelapps.simpleappupdate.config

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes


data class NotificationStyle @JvmOverloads constructor (
    @DrawableRes val notificationIcon: Int,
    @ColorRes val notificationColor: Int? = null,
)
