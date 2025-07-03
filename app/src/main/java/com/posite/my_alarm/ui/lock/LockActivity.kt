package com.posite.my_alarm.ui.lock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.ui.lock.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.main.getNextDate
import com.posite.my_alarm.ui.slide.CircleUnlock
import com.posite.my_alarm.ui.slide.SwipeUnlockButton
import com.posite.my_alarm.util.AlarmReceiver
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_HOUR
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_ID
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MERIDIEM
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MINUTE
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LockActivity : ComponentActivity() {
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    @Inject
    lateinit var alarmManager: AlarmManager

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val id = intent.getLongExtra(ALARM_ID, 0)
        val meridiem = intent.getStringExtra(ALARM_MERIDIEM) ?: getString(R.string.am)
        val hour = intent.getIntExtra(ALARM_HOUR, 0)
        val minute = intent.getIntExtra(ALARM_MINUTE, 0)

        enableEdgeToEdge()
        mediaPlayer.prepare()
        setContent {
            // 뒤로가기 버튼을 눌렀을 때 아무 동작도 하지 않도록 설정
            BackHandler { }
            mediaPlayer.start()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val timings = longArrayOf(100, 200, 100, 200, 100, 200)
                val amplitudes = intArrayOf(0, 50, 0, 100, 0, 200)
                val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
                (vibrator as VibratorManager).vibrate(combinedVibration)
                Log.i("vibrator", "AlarmReceiver onReceive() called 10000 : $vibrator")
            } else {
                val pattern = longArrayOf(0, 3000)
                (vibrator as Vibrator).vibrate(VibrationEffect.createWaveform(pattern, 0))
                Log.i("vibrator", "AlarmReceiver onReceive() called  3000 : $vibrator")
            }
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
                                kotlinx.coroutines.delay(1000) // 1초마다 업데이트
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (vibrator as VibratorManager).cancel()
                        else (vibrator as Vibrator).cancel()
                        val currentDate = Calendar.getInstance()
                        val calendar = getNextDate(
                            AlarmStateEntity(id, hour, minute, meridiem, true),
                            this@LockActivity
                        )
                        if (currentDate.timeInMillis >= calendar.timeInMillis) calendar.add(
                            Calendar.DAY_OF_YEAR,
                            1
                        )
                        mediaPlayer.pause()
                        mediaPlayer.seekTo(0)
                        addAlarm(calendar, id.toInt(), meridiem, hour, minute)
                    }
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun addAlarm(
        calendar: android.icu.util.Calendar,
        id: Int,
        meridiem: String,
        hour: Int,
        minute: Int
    ) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            Intent(this@LockActivity, AlarmReceiver::class.java).putExtra(
                ALARM_ID,
                id
            ).putExtra(ALARM_MERIDIEM, meridiem)
                .putExtra(ALARM_HOUR, hour)
                .putExtra(ALARM_MINUTE, minute).let { intent ->
                    PendingIntent.getBroadcast(
                        this@LockActivity,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
        )
        finish()
    }

    companion object {
        private const val DATE_FORMAT = "yyyy년 MM월 dd일"
        private const val TIME_FORMAT = "aa hh:mm"
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
                    kotlinx.coroutines.delay(1000) // 1초마다 업데이트
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