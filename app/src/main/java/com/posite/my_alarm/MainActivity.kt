package com.posite.my_alarm

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.time.TimePickerDialog
import com.posite.my_alarm.util.AlarmReceiver
import com.posite.my_alarm.util.AlarmReceiver.Companion.TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
        setContent {
            val context = LocalContext.current

            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            MyAlarmTheme {
                val isShowTimePicker = remember { mutableStateOf(true) }
                val meridiemState = remember { PickerState("오전") }
                val hourState = remember { PickerState(0) }
                val minuteState = remember { PickerState("00") }
                if (isShowTimePicker.value) {
                    TimePickerDialog(
                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                        ),
                        onDismissRequest = { isShowTimePicker.value = false },
                        onDoneClickListener = {
                            isShowTimePicker.value = false
                            Toast.makeText(
                                this,
                                "${meridiemState.selectedItem} ${hourState.selectedItem}시 ${minuteState.selectedItem}분",
                                Toast.LENGTH_SHORT
                            ).show()
                            val alarmIntent =
                                Intent(this, AlarmReceiver::class.java).putExtra(TAG, 0)
                                    .let { intent ->
                                        PendingIntent.getBroadcast(
                                            this, 0, intent,
                                            PendingIntent.FLAG_IMMUTABLE
                                        )
                                    }
                            addAlarm(
                                meridiemState = meridiemState.selectedItem,
                                hourState = hourState.selectedItem,
                                minuteState = minuteState.selectedItem,
                                intent = alarmIntent
                            )
                        },
                        meridiemState = meridiemState,
                        hourState = hourState,
                        minuteState = minuteState
                    )
                }

            }
        }
    }

    private fun addAlarm(
        meridiemState: String,
        hourState: Int,
        minuteState: String,
        intent: PendingIntent
    ) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            if (meridiemState == "오후") {
                if (hourState == 12) {
                    set(Calendar.HOUR_OF_DAY, hourState)
                } else {
                    set(Calendar.HOUR_OF_DAY, hourState + 12)
                }
            } else {
                set(Calendar.HOUR_OF_DAY, hourState)
            }
            set(Calendar.MINUTE, minuteState.toInt())
            set(Calendar.SECOND, 0)
        }
        Log.d("MainActivity", "now: ${System.currentTimeMillis()} alarm: ${calendar.timeInMillis}")
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis, intent
        )
        /*alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intent
        )*/
    }

    private fun removeAlarm(intent: PendingIntent) {
        alarmManager.cancel(intent)
        intent.cancel()
    }
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewPicker() {
//    MyAlarmTheme {
//        TimePickerDialog(
//            properties = DialogProperties(
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true,
//            ),
//            onDismissRequest = {},
//            onDoneClickListener = {
//
//            },
//            meridiemState = PickerState("오전"),
//            hourState = PickerState(6),
//            minuteState = PickerState("00")
//        )
//    }
//}

