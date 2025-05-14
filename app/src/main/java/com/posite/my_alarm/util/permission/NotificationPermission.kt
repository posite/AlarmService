package com.posite.my_alarm.util.permission

import android.Manifest
import androidx.activity.ComponentActivity

class NotificationPermission(activity: ComponentActivity) : SinglePermission(activity) {
    override val permission: String = Manifest.permission.POST_NOTIFICATIONS
}