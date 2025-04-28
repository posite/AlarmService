package com.posite.my_alarm.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("Boot completed", "Boot completed")
            val work =
                OneTimeWorkRequestBuilder<AlarmReRegister>().setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            Log.d("BootReceiver", "AlarmReRegister work request created")
            WorkManager.getInstance(context.applicationContext).enqueue(work)
        }
    }
}