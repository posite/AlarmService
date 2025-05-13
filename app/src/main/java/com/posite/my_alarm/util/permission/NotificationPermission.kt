package com.posite.my_alarm.util.permission

import androidx.activity.ComponentActivity

class NotificationPermission(activity: ComponentActivity) : SinglePermission(activity) {
    override val permission: String = "android.permission.POST_NOTIFICATIONS"
}