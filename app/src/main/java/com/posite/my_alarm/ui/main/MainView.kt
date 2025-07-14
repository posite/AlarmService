package com.posite.my_alarm.ui.main

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.icon.Add
import com.posite.my_alarm.icon.Delete
import com.posite.my_alarm.ui.alarm.Alarm
import com.posite.my_alarm.ui.main.MainActivity.Companion.ALARM_MODE_TITLE
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_HOUR
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MERIDIEM
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MINUTE
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MODE_STATE
import com.posite.my_alarm.ui.picker.TimePickerDialog
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun MainView(
    alarmManager: AlarmManager,
    isDeleteMode: MutableState<Boolean>,
    isAlarmClick: MutableState<AlarmStateEntity?>,
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    states: MainContract.MainUiState,
    onSwitchChanges: (Boolean, AlarmStateEntity) -> Unit,
    deleteAlarm: (AlarmStateEntity) -> Unit,
    updateAlarm: () -> Unit,
    insertAlarm: () -> Unit,
) {
    val context = LocalContext.current
    var minTime by remember { mutableStateOf<AlarmStateEntity?>(null) }
    val isShowTimePicker = remember { mutableStateOf(DEFAULT_MODE_STATE) }
    val set = mutableSetOf<AlarmStateEntity>()

    var isTitleVisible by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return if (delta < 0 && scrollState.value < scrollState.maxValue) {
                    // 위로 스크롤할 때, 상단 영역 스크롤이 최대치에 도달하지 않았다면 상단 영역을 스크롤
                    isTitleVisible = false
                    scrollState.dispatchRawDelta(-delta)
                    Offset(0f, delta)
                } else if (delta > 0 && scrollState.value > 0) {
                    // 아래로 스크롤할 때, 상단 영역이 완전히 보이지 않는다면 상단 영역을 스크롤
                    isTitleVisible = true
                    val consume = minOf(scrollState.value.toFloat(), delta)
                    scrollState.dispatchRawDelta(-consume)
                    Offset(0f, consume)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LaunchedEffect(states) {
            Log.d("MainActivity", "states: ${states.alarmList}")
            while (true) {
                delay(1000)
                minTime = if (states.alarmList.isEmpty().not()) {
                    states.alarmList.filter { it.isActive }
                        .minByOrNull { getNextDate(it, context).timeInMillis }
                } else null
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
        MinRemainAlarm(scrollState.value, minTime)

        Spacer(modifier = Modifier.height(24.dp))
        AlarmList(
            states.alarmList,
            isDeleteMode,
            set,
            isShowTimePicker,
            nestedScrollConnection,
            isAlarmClick,
            onSwitchChanges = { isActive, item ->
                onSwitchChanges(isActive, item)
            },
            onRemoveAlarm = { alarm ->
                deleteAlarm(alarm)
            }
        )

        if (isAlarmClick.value != null) {
            meridiemState.selectedItem = isAlarmClick.value!!.meridiem
            hourState.selectedItem = isAlarmClick.value!!.hour
            minuteState.selectedItem = isAlarmClick.value!!.minute.toString()
            TimePickerDialog(
                meridiem = isAlarmClick.value!!.meridiem,
                hour = isAlarmClick.value!!.hour,
                minute = isAlarmClick.value!!.minute,
                modifier = Modifier.background(color = Color.White),
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
                onDismissRequest = { isAlarmClick.value = null },
                onDoneClickListener = {
                    updateAlarm()
                    isAlarmClick.value = null
                },
                meridiemState = meridiemState,
                hourState = hourState,
                minuteState = minuteState
            )
        }
        if (isShowTimePicker.value) {
            meridiemState.selectedItem = DEFAULT_MERIDIEM
            hourState.selectedItem = DEFAULT_HOUR
            minuteState.selectedItem = DEFAULT_MINUTE
            TimePickerDialog(
                modifier = Modifier.background(color = Color.White),
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
                onDismissRequest = { isShowTimePicker.value = false },
                onDoneClickListener = {
                    isShowTimePicker.value = false
                    insertAlarm()
                },
                meridiemState = meridiemState,
                hourState = hourState,
                minuteState = minuteState,
                meridiem = DEFAULT_MERIDIEM,
                hour = DEFAULT_HOUR,
                minute = DEFAULT_MINUTE.toInt()
            )
        }
    }
}


@Composable
fun MinRemainAlarm(
    scrollValue: Int,
    minTime: AlarmStateEntity?
) {
    val alpha = 1f - (scrollValue.toFloat() / 350f).coerceIn(0f, 1f)
    var remainHour by remember { mutableStateOf(0) }
    var remainMinute by remember { mutableStateOf(0) }
    val context = LocalContext.current

    if (minTime != null) {
        LaunchedEffect(minTime) {
            /*while (true) {
                delay(1000)
                val time = checkTimeChange(minTime)
                remainHour = time.first
                remainMinute = time.second
            }*/
            val time = checkTimeChange(minTime, context)
            remainHour = time.first
            remainMinute = time.second
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
        val date = getNextDate(minTime, context)
        val meridiem =
            if (date.get(Calendar.AM_PM) == Calendar.AM) stringResource(R.string.am) else stringResource(
                R.string.pm
            )
        Text(
            text = "${date.get(Calendar.MONTH) + 1}월 ${date.get(Calendar.DAY_OF_MONTH)}일 $meridiem (${
                getDayOfWeek(
                    date.get(Calendar.DAY_OF_WEEK)
                )
            }) ${
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

private fun checkTimeChange(minTime: AlarmStateEntity, context: Context): Pair<Int, Int> {
    val localDateTime = LocalDateTime.now()
    var hour = minTime.hour - localDateTime.hour
    if (minTime.meridiem == context.getString(R.string.pm)) {
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
        val selectedAlarms = remember { mutableStateOf(mutableSetOf<AlarmStateEntity>()) }
        val isSelectedAll = remember(selectedAlarms.value) {
            derivedStateOf {
                alarmList.isNotEmpty() && selectedAlarms.value.size == alarmList.size
            }
        }
        selectedAlarms.value = set
        AlarmListTitle(
            selectedAlarms,
            alarmList,
            isDeleteMode,
            set,
            isShowTimePicker,
            isSelectedAll,
            onRemoveAlarm
        )

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
                        isSelected = selectedAlarms.value.contains(item),
                        onAlarmSelected = {
                            set.add(item)
                            val newSet = selectedAlarms.value.toMutableSet()
                            newSet.add(item)
                            selectedAlarms.value = newSet
                            //isSelected.value = true
                            Log.d("MainActivity", "set: $set")
                        },
                        onAlarmUnselected = {
                            set.remove(item)
                            val newSet = selectedAlarms.value.toMutableSet()
                            newSet.remove(item)
                            selectedAlarms.value = newSet
                            //isSelected.value = false
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
    selectedAlarms: MutableState<MutableSet<AlarmStateEntity>>,
    alarmList: List<AlarmStateEntity>,
    isDeleteMode: MutableState<Boolean>,
    set: MutableSet<AlarmStateEntity>,
    isShowTimePicker: MutableState<Boolean>,
    isSelectedAll: State<Boolean>,
    removeAlarm: (AlarmStateEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDeleteMode.value) {
            Column(
                modifier = Modifier.selectable(
                    selected = isSelectedAll.value,
                    onClick = {
                        if (isSelectedAll.value) {
                            selectedAlarms.value = mutableSetOf()
                            set.clear()
                            Log.d("MainActivity", "set: $set")
                            Log.d("MainActivity", "selectedAlarms: ${selectedAlarms.value}")
                            Log.d("MainActivity", "isSelectedAll: ${isSelectedAll.value}")
                        } else {
                            selectedAlarms.value = alarmList.toMutableSet()
                            set.addAll(alarmList)
                            Log.d("MainActivity", "set: $set")
                            Log.d("MainActivity", "selectedAlarms: ${selectedAlarms.value}")
                            Log.d("MainActivity", "isSelectedAll: ${isSelectedAll.value}")
                        }
                    },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(
                    modifier = Modifier.size(30.dp),
                    selected = isSelectedAll.value,
                    onClick = {
                        if (isSelectedAll.value) {
                            selectedAlarms.value = mutableSetOf()
                            set.clear()
                            Log.d("MainActivity", "set: $set")
                            Log.d("MainActivity", "selectedAlarms: ${selectedAlarms.value}")
                            Log.d("MainActivity", "isSelectedAll: ${isSelectedAll.value}")
                        } else {
                            selectedAlarms.value = alarmList.toMutableSet()
                            set.addAll(alarmList)
                            Log.d("MainActivity", "set: $set")
                            Log.d("MainActivity", "selectedAlarms: ${selectedAlarms.value}")
                            Log.d("MainActivity", "isSelectedAll: ${isSelectedAll.value}")
                        }
                    }
                )
                Text(
                    text = "전체",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        } else {
            Text(
                text = ALARM_MODE_TITLE,
                fontSize = 24.sp,
                fontWeight = Bold
            )
        }

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

fun getNextDate(alarm: AlarmClockInfo, context: Context): Calendar {
    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = alarm.triggerTime
    }
    //Log.d("MainActivity", "calendar: ${calendar.get(Calendar.HOUR_OF_DAY)}")
    if (System.currentTimeMillis() >= calendar.timeInMillis) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar
}

fun getNextDate(alarm: AlarmStateEntity, context: Context): Calendar {
    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        if (alarm.meridiem == context.getString(R.string.pm)) {
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

fun getDayOfWeek(day: Int): String {
    return when (day) {
        1 -> "일"
        2 -> "월"
        3 -> "화"
        4 -> "수"
        5 -> "목"
        6 -> "금"
        7 -> "토"
        else -> ""
    }
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