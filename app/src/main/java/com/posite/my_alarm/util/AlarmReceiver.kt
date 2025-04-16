package com.posite.my_alarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import com.posite.my_alarm.ui.lock.LockActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ALARM_ID, 0)
        val meridiem = intent.getStringExtra(ALARM_MERIDIEM) ?: "오전"
        val hour = intent.getIntExtra(ALARM_HOUR, 0)
        val minute = intent.getIntExtra(ALARM_MINUTE, 0)
        context.startActivity(
            Intent(context, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        )

        setAlarmForNextDay(context, id, meridiem, hour, minute)
    }

    private fun setAlarmForNextDay(
        context: Context,
        id: Long,
        meridiem: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            if (meridiem == "오후") {
                if (hour == 12) {
                    set(Calendar.HOUR_OF_DAY, hour)
                } else {
                    set(Calendar.HOUR_OF_DAY, hour + 12)
                }
            } else {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    companion object {
        const val ALARM_ID = "ID"
        const val ALARM_MERIDIEM = "MERIDIEM"
        const val ALARM_HOUR = "HOUR"
        const val ALARM_MINUTE = "MINUTE"
    }
}