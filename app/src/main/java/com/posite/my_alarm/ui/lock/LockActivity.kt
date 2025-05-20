package com.posite.my_alarm.ui.lock

import android.content.Context
import android.content.pm.ActivityInfo
import android.icu.text.SimpleDateFormat
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
import com.posite.my_alarm.ui.lock.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.slide.CircleUnlock
import com.posite.my_alarm.ui.slide.SwipeUnlockButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class LockActivity : ComponentActivity() {
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        enableEdgeToEdge()
        setContent {
            // 뒤로가기 버튼을 눌렀을 때 아무 동작도 하지 않도록 설정
            BackHandler { }

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

                        finish()
                    }
                }
            }
        }
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