package com.posite.my_alarm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reqCode = intent.extras?.getInt(TAG)
        Log.i("AlarmReceiver", "AlarmReceiver onReceive() called : $reqCode")
    }

    companion object {
        const val TAG = "AlarmReceiver"
    }
}