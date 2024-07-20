package com.triskelapps.updateappview.config

import androidx.annotation.ColorRes
import androidx.annotation.StyleRes

data class UpdateBarStyle(
    @ColorRes val backgroundColor: Int,
    @ColorRes val foregroundElementsColor: Int,
    @StyleRes val textStyle: Int?,
)
