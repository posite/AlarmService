package com.posite.my_alarm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val work = OneTimeWorkRequestBuilder<AlarmReRegister>().build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}