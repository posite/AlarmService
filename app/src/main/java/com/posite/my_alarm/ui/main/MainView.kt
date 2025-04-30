package com.posite.my_alarm.ui.main

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.icon.Add
import com.posite.my_alarm.icon.Delete
import com.posite.my_alarm.ui.alarm.Alarm
import com.posite.my_alarm.ui.main.MainActivity.Companion.ALARM_MODE_TITLE
import com.posite.my_alarm.ui.main.MainActivity.Companion.DELETE_MODE_TITLE
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun MinRemainAlarm(
    scrollState: ScrollState,
    minTime: AlarmStateEntity?
) {
    val alpha = 1f - (scrollState.value.toFloat() / 350f).coerceIn(0f, 1f)
    var remainHour by remember { mutableStateOf(0) }
    var remainMinute by remember { mutableStateOf(0) }

    if (minTime != null) {
        Log.d("MainActivity", "minTime: $minTime")
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                val time = checkTimeChange(minTime)
                remainHour = time.first
                remainMinute = time.second
            }
        }

        Text(
            text = "${remainHour}시간 ${remainMinute}분 후에",
            modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 0.dp),
            fontSize = 32.sp,
            color = Color.Black.copy(alpha)
        )

        Text(
            text = "알람이 울립니다.",
            fontSize = 32.sp,
            color = Color.Black.copy(alpha)
        )
        val date = getNextDate(minTime)
        val meridiem = if (date.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        Text(
            text = "${date.get(Calendar.MONTH) + 1}월 ${date.get(Calendar.DAY_OF_MONTH)}일 $meridiem ${
                stringResource(
                    R.string.alarm_time,
                    if (date.get(Calendar.HOUR_OF_DAY) == 0) 12 else date.get(Calendar.HOUR),
                    date.get(Calendar.MINUTE)
                )
            }",
            fontSize = 16.sp,
            modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 0.dp),
            color = Color.Gray.copy(alpha)
        )
    } else {
        Text(
            text = "설정된 알람이 없습니다.",
            fontSize = 32.sp,
            modifier = Modifier.padding(0.dp, 16.dp),
            color = Color.Black.copy(alpha)
        )
    }
}

private fun checkTimeChange(minTime: AlarmStateEntity): Pair<Int, Int> {
    val localDateTime = LocalDateTime.now()
    var hour = minTime.hour - localDateTime.hour
    if (minTime.meridiem == "오후") {
        hour += 12
    }
    var minute = minTime.minute - localDateTime.minute
    if (minute < 0) {
        hour -= 1
        minute += 60
    }
    if (hour < 0) {
        hour += 24
    }

    return Pair(hour, minute)
}

@Composable
fun AlarmList(
    alarmList: List<AlarmStateEntity>,
    isDeleteMode: MutableState<Boolean>,
    set: MutableSet<AlarmStateEntity>,
    isShowTimePicker: MutableState<Boolean>,
    nestedScrollConnection: NestedScrollConnection,
    isAlarmClick: MutableState<AlarmStateEntity?>,
    onSwitchChanges: (Boolean, AlarmStateEntity) -> Unit,
    onRemoveAlarm: (AlarmStateEntity) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 40.dp, 0.dp, 12.dp)
            .background(color = Color.White)
    ) {
        AlarmListTitle(isDeleteMode, set, isShowTimePicker, onRemoveAlarm)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenHeightDp.dp * 0.9f)
                .background(color = Color.White)
                .nestedScroll(nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.padding(12.dp, 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = alarmList, key = { it.id }) { item ->
                    //Log.d("MainActivity", "item: $item")
                    Alarm(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            ),
                            fadeOutSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ),
                        alarm = item,
                        isDeleteMode = isDeleteMode,
                        onAlarmSelected = {
                            set.add(item)
                            Log.d("MainActivity", "set: $set")
                        },
                        onAlarmUnselected = {
                            set.remove(it)
                            Log.d("MainActivity", "set: $set")
                        },
                        onSwitchChanges = {
                            onSwitchChanges(it, item)
                        },
                        onAlarmClicked = {
                            isAlarmClick.value = item
                            Log.d(
                                "MainActivity",
                                "isAlarmClick: ${isAlarmClick.value}"
                            )
                        })
                }
            }
        }
    }
}

@Composable
fun AlarmListTitle(
    isDeleteMode: MutableState<Boolean>,
    set: MutableSet<AlarmStateEntity>,
    isShowTimePicker: MutableState<Boolean>,
    removeAlarm: (AlarmStateEntity) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDeleteMode.value) DELETE_MODE_TITLE else ALARM_MODE_TITLE,
            fontSize = 24.sp,
            fontWeight = Bold
        )
        IconButton(
            modifier = Modifier.background(
                shape = CircleShape,
                color = Color.Transparent
            ),
            onClick = {
                if (isDeleteMode.value) {
                    Log.d("MainActivity", "set: $set")
                    for (alarm in set) {
                        removeAlarm(alarm)
                    }
                    set.clear()
                    isDeleteMode.value = false
                } else {
                    isShowTimePicker.value = true
                }
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                tint = Color.Black,
                contentDescription = "Add",
                imageVector = if (isDeleteMode.value) Delete else Add
            )
        }
    }
}

fun getNextDate(alarm: AlarmStateEntity): Calendar {
    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        if (alarm.meridiem == "오후") {
            if (alarm.hour == 12) {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
            } else {
                set(Calendar.HOUR_OF_DAY, alarm.hour + 12)
            }
        } else {
            if (alarm.hour == 12) {
                set(Calendar.HOUR_OF_DAY, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
            }
        }
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
    }
    //Log.d("MainActivity", "calendar: ${calendar.get(Calendar.HOUR_OF_DAY)}")
    if (System.currentTimeMillis() >= calendar.timeInMillis) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar
}

@Preview(showBackground = true)
@Composable
fun IconPreview() {
    IconButton(
        modifier = Modifier.background(
            shape = CircleShape,
            color = Color.Transparent
        ),
        onClick = { }
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            tint = Color.Black,
            contentDescription = "Add",
            imageVector = Add
        )
    }
}