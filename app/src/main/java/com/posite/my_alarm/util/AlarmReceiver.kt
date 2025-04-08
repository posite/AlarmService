package com.posite.my_alarm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.posite.my_alarm.ui.lock.LockActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startActivity(
            Intent(context, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        )


    }

    companion object {
        const val TAG = "AlarmReceiver"
    }
}