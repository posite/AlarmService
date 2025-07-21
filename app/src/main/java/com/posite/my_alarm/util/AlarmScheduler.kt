package com.posite.my_alarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity

object AlarmScheduler {
    fun clearAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancelAll()
    }

    fun setExactAlarm(context: Context, alarm: AlarmStateEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            if (alarm.meridiem == context.getString(R.string.pm)) {
                if (alarm.hour == 12) {
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                } else {
                    set(Calendar.HOUR_OF_DAY, alarm.hour + 12)
                }
            } else {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
            }
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }
        val current = System.currentTimeMillis()
        if (current > calendar.timeInMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}