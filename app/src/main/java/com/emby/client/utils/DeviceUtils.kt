package com.emby.client.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build

object DeviceUtils {
    fun isTv(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
            uiMode == Configuration.UI_MODE_TYPE_TELEVISION
        } else {
            false
        }
    }
}
