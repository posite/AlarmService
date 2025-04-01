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
        /*val reqCode = intent.extras?.getInt(TAG)
        Log.i("AlarmReceiver", "AlarmReceiver onReceive() called : $reqCode")
        val builder = NotificationCompat.Builder(context, "alarm")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Alarm")
            .setVibrate(longArrayOf(0, 5000, 5000, 5000))
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
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(0, 5000, 5000, 5000)
        manager.createNotificationChannel(channel)
        val notification = builder.build()
        manager.notify(1, notification)*/
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val timings = longArrayOf(100, 200, 100, 200, 100, 200)
            val amplitudes = intArrayOf(0, 50, 0, 100, 0, 200)
            val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, 0)
            val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
            vibrator.vibrate(combinedVibration)
            Log.i("vibrator", "AlarmReceiver onReceive() called 10000 : $vibrator")
        } else {
            val vibrator = getSystemService(context, Vibrator::class.java)
            val pattern = longArrayOf(0, 3000)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            Log.i("vibrator", "AlarmReceiver onReceive() called  3000 : $vibrator")
        }
        */

    }

    companion object {
        const val TAG = "AlarmReceiver"
    }
}