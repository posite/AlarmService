package com.posite.my_alarm.ui.lock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ActivityInfo
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.posite.my_alarm.AlarmApplication
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.ui.lock.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.main.MainActivity.Companion.DAY_OF_WEEKS
import com.posite.my_alarm.ui.main.MainActivity.Companion.VERSION_CODE
import com.posite.my_alarm.ui.picker.DayOfWeek
import com.posite.my_alarm.ui.slide.CircleUnlock
import com.posite.my_alarm.ui.slide.SwipeUnlockButton
import com.posite.my_alarm.util.AlarmReceiver
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_HOUR
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_ID
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MERIDIEM
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MINUTE
import com.posite.my_alarm.util.AlarmSoundPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class LockActivity : ComponentActivity() {
    private val viewModel by viewModels<LockViewModel>()
    private val vibrator by lazy {
        getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
    }
    private lateinit var isAlarmValid: MutableState<Boolean>
    private var date: DayOfWeek? = null

    @Inject
    lateinit var alarmManager: AlarmManager

    @Inject
    lateinit var alarmSoundPlayer: AlarmSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val id = intent.getLongExtra(ALARM_ID, 0)
        val meridiem = intent.getStringExtra(ALARM_MERIDIEM) ?: getString(R.string.am)
        val hour = intent.getIntExtra(ALARM_HOUR, 0)
        val minute = intent.getIntExtra(ALARM_MINUTE, 0)
        val ordinals = intent.getIntExtra(DAY_OF_WEEKS, -1)
        if (ordinals == -1) finish()
        date = DayOfWeek.entries[ordinals]
        onEffect()
        enableEdgeToEdge()
        Log.d(
            "LockActivity",
            "onCreate: id=$id, hour=$hour, minute=$minute, meridiem=$meridiem date=$date"
        )
        setContent {
            BackHandler { }
            isAlarmValid = remember { mutableStateOf(false) }
            viewModel.getAlarmStateById(id)
            if (isAlarmValid.value) {
                RingAlarm(
                    id = id,
                    hour = hour,
                    minute = minute,
                    meridiem = meridiem,
                )
            }
        }
    }

    private fun onEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect {
                    when (it) {
                        is LockContract.LockEffect.IsAlarmExist -> {
                            val alarmState = it.alarm
                            if (checkValidAlarm(
                                    alarmState.id,
                                    alarmState.hour,
                                    alarmState.minute,
                                    alarmState.meridiem,
                                    alarmState
                                )
                            ) {
                                isAlarmValid.value = true
                            } else {
                                finish()
                            }
                        }

                        else -> {
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun checkValidAlarm(
        id: Long,
        hour: Int,
        minute: Int,
        meridiem: String,
        alarm: AlarmStateEntity
    ) = id == alarm.id &&
            hour == alarm.hour &&
            minute == alarm.minute &&
            meridiem == alarm.meridiem &&
            alarm.isActive &&
            alarm.dayOfWeeks.contains(date)


    @Composable
    private fun RingAlarm(
        id: Long,
        hour: Int,
        minute: Int,
        meridiem: String
    ) {
        alarmSoundPlayer.play()
        val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, 0)
        val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
        vibrator.vibrate(combinedVibration)
        Log.i("vibrator", "AlarmReceiver onReceive() called 10000 : $vibrator")
        MyAlarmTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Transparent),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Transparent),
                    //.background(color = Color.White)
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var currentTime by remember { mutableStateOf("df") }
                    var currentDate by remember { mutableStateOf("df") }

                    LaunchedEffect(Unit) {
                        while (true) {
                            currentDate =
                                SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(
                                    Date()
                                )
                            currentTime =
                                SimpleDateFormat(
                                    TIME_FORMAT,
                                    Locale.getDefault()
                                ).format(Date())
                            delay(1000) // 1초마다 업데이트
                        }
                    }

                    // 현재 날짜 표시
                    Text(
                        text = currentDate,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // 현재 시간 표시 (크고 중앙에 배치)
                    Text(
                        text = currentTime, // 현재 시간 텍스트
                        fontSize = 32.sp,
                        modifier = Modifier.padding(vertical = 16.dp) // 위아래 패딩 추가
                    )
                }
                /*SwipeUnlockButton {
                        Log.d("LockActivity", "SwipeUnlockButton clicked")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (vibrator as VibratorManager).cancel()
                        else (vibrator as Vibrator).cancel()

                        finish()
                    }*/
                CircleUnlock {
                    vibrator.cancel()
                    val calendar =
                        getNextWeekDate(AlarmStateEntity(id, hour, minute, meridiem, true))
                    alarmSoundPlayer.stop()
                    updateAlarm(calendar, id.toInt(), meridiem, hour, minute)
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun updateAlarm(
        calendar: Calendar,
        id: Int,
        meridiem: String,
        hour: Int,
        minute: Int,
    ) {
        alarmManager.cancel(
            Intent(this@LockActivity, AlarmReceiver::class.java).putExtra(ALARM_ID, id)
                .putExtra(ALARM_MERIDIEM, meridiem)
                .putExtra(ALARM_HOUR, hour)
                .putExtra(ALARM_MINUTE, minute)
                .putExtra(ALARM_MINUTE, minute).putExtra(
                    VERSION_CODE,
                    packageManager.getPackageInfo(packageName, 0).longVersionCode
                ).putExtra(DAY_OF_WEEKS, date!!.ordinal).let { intent ->
                    PendingIntent.getBroadcast(
                        this@LockActivity,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                }
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            Intent(this@LockActivity, AlarmReceiver::class.java).putExtra(ALARM_ID, id)
                .putExtra(ALARM_MERIDIEM, meridiem).putExtra(ALARM_HOUR, hour)
                .putExtra(ALARM_MINUTE, minute).putExtra(
                    VERSION_CODE,
                    packageManager.getPackageInfo(packageName, 0).longVersionCode
                ).putExtra(DAY_OF_WEEKS, date!!.ordinal).let { intent ->
                    PendingIntent.getBroadcast(
                        this@LockActivity,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                }
        )
        finish()
    }

    @OptIn(ExperimentalTime::class)
    fun getNextWeekDate(alarm: AlarmStateEntity): Calendar {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val currentDateTime = now.toLocalDateTime(timeZone)

        val hour24 = if (alarm.meridiem == AlarmApplication.Companion.getString(R.string.pm)) {
            if (alarm.hour == 12) alarm.hour else alarm.hour + 12
        } else {
            if (alarm.hour == 12) 0 else alarm.hour
        }

        val alarmTime = LocalTime(hour24, alarm.minute, 0)
        var alarmDateTime = currentDateTime.date.atTime(alarmTime)
        alarmDateTime = alarmDateTime.date.plus(7, DateTimeUnit.DAY).atTime(alarmTime)

        return Calendar.getInstance().apply {
            val instant = alarmDateTime.toInstant(timeZone)
            timeInMillis = instant.epochSeconds * 1000 + instant.nanosecondsOfSecond / 1_000_000
        }
    }

    companion object {
        private const val DATE_FORMAT = "yyyy년 MM월 dd일"
        private const val TIME_FORMAT = "aa hh:mm"
        private val timings = longArrayOf(100, 200, 100, 200, 100, 200)
        private val amplitudes = intArrayOf(0, 50, 0, 100, 0, 200)
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyAlarmTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent),
            //.background(color = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Transparent),
                //.background(color = Color.White)
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var currentTime by remember { mutableStateOf("df") }
                var currentDate by remember { mutableStateOf("df") }

                LaunchedEffect(Unit) {
                    currentDate =
                        SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(
                            Date()
                        )
                    currentTime =
                        SimpleDateFormat("aa hh:mm", Locale.getDefault()).format(Date())
                    delay(1000) // 1초마다 업데이트
                }

                // 현재 날짜 표시
                Text(
                    text = currentDate,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // 현재 시간 표시 (크고 중앙에 배치)
                Text(
                    text = currentTime, // 현재 시간 텍스트
                    fontSize = 32.sp,
                    modifier = Modifier.padding(vertical = 16.dp) // 위아래 패딩 추가
                )
            }
            CircleUnlock {

            }
            SwipeUnlockButton {

            }
        }

    }
}