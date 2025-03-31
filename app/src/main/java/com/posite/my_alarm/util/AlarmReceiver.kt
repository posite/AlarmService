package com.posite.my_alarm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.posite.my_alarm.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reqCode = intent.extras?.getInt(TAG)
        Log.i("AlarmReceiver", "AlarmReceiver onReceive() called : $reqCode")
        val builder = NotificationCompat.Builder(context, "alarm")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Alarm")
            .setContentText("Alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "alarm",
            "Alarm 서비스!!",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
        val notification = builder.build()
        manager.notify(1, notification)
    }

    companion object {
        const val TAG = "AlarmReceiver"
    }
}