package com.posite.my_alarm.util.permission

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity

class ExactAlarmPermission(activity: ComponentActivity) : SinglePermission(activity) {
    override val permission: String = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
    private val context: Activity = activity

    override fun request() {
        val intent = Intent(permission)
        context.startActivity(intent)
    }
}