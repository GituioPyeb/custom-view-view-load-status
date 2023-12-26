package com.example.viewloadstatuslibs.view

import android.content.Context
import android.content.res.Configuration

object Utils {
    fun isDarkModeEnabled(context: Context): Boolean {
        // 获取当前的模式
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // 检查当前的模式是否为 UI_MODE_NIGHT_YES（表示启用了暗色模式）
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}