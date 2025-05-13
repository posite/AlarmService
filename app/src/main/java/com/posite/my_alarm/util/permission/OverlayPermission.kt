package com.posite.my_alarm.util.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity

class OverlayPermission(activity: ComponentActivity) : SinglePermission(activity) {
    override val permission: String = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
    private val context: Activity = activity

    override fun request() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivityForResult(intent, 0)
    }
}