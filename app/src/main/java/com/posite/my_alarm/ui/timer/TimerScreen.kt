package com.posite.my_alarm.ui.timer

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.R
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_HOUR_STRING
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MINUTE
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_SECOND
import com.posite.my_alarm.ui.picker.CountDownTimerPicker
import com.posite.my_alarm.ui.theme.LightSkyBlue
import com.posite.my_alarm.ui.theme.SkyBlue
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@Composable
fun TimerScreen(
    uiState: TimerContract.TimerUiState,
    onTimerSet: (Int) -> Unit,
    onTimerTikTok: () -> Unit,
    onTimerDelete: () -> Unit,
    onTimerPause: () -> Unit,
    onTimerRestart: () -> Unit
) {
    Log.d("TimerScreen", "uiState: ${uiState.isRunning}")
    LaunchedEffect(uiState.remainTime, uiState.isRunning) {
        //Log.d("LaunchedEffect", "uiState: ${uiState.isRunning}")
        delay(910)
        if (uiState.isRunning == TimerContract.TimerState.Running) {
            onTimerTikTok()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (uiState.remainTime > 0 && uiState.initialTime > 0) {
            DrawTimer(uiState, onTimerDelete, onTimerPause, onTimerRestart)
        } else {
            SettingTimeScreen(onTimerSet)
        }
    }
}

@Composable
private fun SettingTimeScreen(onTimerSet: (Int) -> Unit) {
    val hourState = remember { PickerState(DEFAULT_HOUR_STRING) }
    val minuteState = remember { PickerState(DEFAULT_MINUTE) }
    val secondState = remember { PickerState(DEFAULT_SECOND) }

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) Arrangement.SpaceAround else Arrangement.Top
    ) {
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Spacer(
            modifier = Modifier.size(60.dp)
        )
        CountDownTimerPicker(hourState, minuteState, secondState)
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Spacer(
            modifier = Modifier.size(24.dp)
        )
        Button(
            onClick = { onTimerSet(hourState.selectedItem.toInt() * 3600 + minuteState.selectedItem.toInt() * 60 + secondState.selectedItem.toInt()) },
            modifier = Modifier
                .width(160.dp)
                .padding(0.dp, 2.dp)
        ) {
            Text(
                text = "시작", fontSize = 24.sp
            )
        }
    }

}

@OptIn(ExperimentalTime::class)
@Composable
private fun DrawTimer(
    uiState: TimerContract.TimerUiState,
    onTimerDelete: () -> Unit,
    onTimerPause: () -> Unit,
    onTimerRestart: () -> Unit
) {
    val progress = remember { Animatable(uiState.remainTime / uiState.initialTime.toFloat()) }
    val initialTime = if (uiState.initialTime / 3600 >= 1) {
        stringResource(
            R.string.initial_time_hour,
            uiState.initialTime / 3600,
            (uiState.initialTime % 3600) / 60,
            uiState.initialTime % 60
        )
    } else if (uiState.initialTime / 60 >= 1) {
        stringResource(
            R.string.initial_time_minute,
            (uiState.initialTime % 3600) / 60,
            uiState.initialTime % 60
        )
    } else {
        stringResource(R.string.initial_time_second, uiState.initialTime % 60)
    }

    val now = Clock.System.now() + uiState.remainTime.seconds
    val resultTime = now.toLocalDateTime(TimeZone.currentSystemDefault())

    val remainTime = if (uiState.remainTime / 3600 >= 1) {
        stringResource(
            R.string.count_time_hour,
            uiState.remainTime / 3600,
            (uiState.remainTime % 3600) / 60,
            uiState.remainTime % 60
        )
    } else if (uiState.remainTime / 60 >= 1) {
        stringResource(
            R.string.count_time_minute,
            (uiState.remainTime % 3600) / 60,
            uiState.remainTime % 60
        )
    } else {
        stringResource(R.string.count_time_second, uiState.remainTime % 60)
    }

    LaunchedEffect(uiState.isRunning) {
        Log.d("DrawTimer", "uiState: ${uiState.isRunning}")
        if (uiState.isRunning == TimerContract.TimerState.Running) {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = uiState.remainTime * 1000,
                    easing = LinearEasing
                )
            )
        } else {
            progress.stop()
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 0.dp, 0.dp, 0.dp),
        verticalArrangement = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) Arrangement.SpaceAround else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 400.dp else 150.dp),
            contentAlignment = Alignment.Center
        ) {

            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                CircularProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.size(350.dp),
                    color = if (uiState.isRunning == TimerContract.TimerState.Running) SkyBlue else Color.LightGray,
                    strokeWidth = 16.dp,
                    trackColor = LightSkyBlue,
                    strokeCap = StrokeCap.Round,
                )
            }

            Column(
                modifier = Modifier
                    .size(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 300.dp else 150.dp)
                    .padding(
                        0.dp,
                        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 32.dp else 0.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = initialTime, fontSize = 24.sp)
                Text(text = remainTime, fontSize = 48.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(
                            R.string.result_time,
                            if (resultTime.hour < 12) "오전" else "오후",
                            if (resultTime.hour <= 12) resultTime.hour else resultTime.hour - 12,
                            resultTime.minute
                        ), fontSize = 16.sp
                    )
                }

            }
        }

        if (uiState.remainTime >= 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onTimerDelete() },
                    modifier = Modifier
                        .width(160.dp)
                        .padding(0.dp, 2.dp)
                ) {
                    Text(text = "삭제", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.size(8.dp))

                if (uiState.isRunning == TimerContract.TimerState.Running) {
                    Button(
                        onClick = { onTimerPause() },
                        modifier = Modifier
                            .width(160.dp)
                            .padding(0.dp, 2.dp)
                    ) {
                        Text(text = "일시정지", fontSize = 24.sp)
                    }
                } else {
                    Button(
                        onClick = { onTimerRestart() },
                        modifier = Modifier
                            .width(160.dp)
                            .padding(0.dp, 2.dp)
                    ) {
                        Text(text = "재시작", fontSize = 24.sp)
                    }
                }
            }
        }
    }

}