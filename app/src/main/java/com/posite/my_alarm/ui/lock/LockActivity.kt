package com.posite.my_alarm.ui.lock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import com.posite.my_alarm.ui.lock.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.slide.SwipeUnlockButton
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class LockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        enableEdgeToEdge()
        setContent {
            // 뒤로가기 버튼을 눌렀을 때 아무 동작도 하지 않도록 설정
            BackHandler { }

            MyAlarmTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center, // 중앙 정렬
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        var currentTime by remember { mutableStateOf("") }
                        var currentDate by remember { mutableStateOf("") }

                        LaunchedEffect(Unit) {
                            while (true) {
                                // 현재 시간 업데이트
                                currentTime =
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                                        Date()
                                    )
                                currentDate =
                                    SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(
                                        Date()
                                    )
                                kotlinx.coroutines.delay(1000) // 1초마다 업데이트
                            }
                        }

                        // 현재 날짜 표시
                        Text(
                            text = currentDate, // 현재 날짜 텍스트
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp) // 위아래 패딩 추가
                        )

                        // 현재 시간 표시 (크고 중앙에 배치)
                        Text(
                            text = currentTime, // 현재 시간 텍스트
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize * 2 // 텍스트 크기 조정
                            ),
                            modifier = Modifier.padding(vertical = 16.dp) // 위아래 패딩 추가
                        )
                    }
                    SwipeUnlockButton() {
                        finish()
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyAlarmTheme {
        SwipeUnlockButton()
    }
}