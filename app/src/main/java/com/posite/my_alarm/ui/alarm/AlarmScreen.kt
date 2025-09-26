package com.posite.my_alarm.ui.alarm

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.posite.my_alarm.AlarmApplication.Companion.getString
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.icon.Add
import com.posite.my_alarm.icon.Delete
import com.posite.my_alarm.ui.main.MainActivity.Companion.ALARM_MODE_TITLE
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_HOUR
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MERIDIEM
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MINUTE
import com.posite.my_alarm.ui.main.MainActivity.Companion.DEFAULT_MODE_STATE
import com.posite.my_alarm.ui.picker.DayOfWeek
import com.posite.my_alarm.ui.picker.TimePickerDialog
import kotlinx.coroutines.delay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun AlarmScreen(
    isDeleteMode: MutableState<Boolean>,
    isAlarmClick: MutableState<AlarmStateEntity?>,
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    selectedDayOfWeek: MutableSet<DayOfWeek>,
    states: AlarmContract.AlarmUiState,
    onSwitchChanges: (Boolean, AlarmStateEntity) -> Unit,
    deleteAlarm: (AlarmStateEntity) -> Unit,
    updateAlarm: (AlarmStateEntity) -> Unit,
    insertAlarm: () -> Unit,
    calculateMinTime: (RemainTime?) -> Unit
) {
    Log.d("MainActivity", states.minTime.toString())
    val isShowTimePicker = remember { mutableStateOf(DEFAULT_MODE_STATE) }
    val set = mutableSetOf<AlarmStateEntity>()

    val scrollState = rememberScrollState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return if (delta < 0 && scrollState.value < scrollState.maxValue) {
                    scrollState.dispatchRawDelta(-delta)
                    Offset(0f, delta)
                } else if (delta > 0 && scrollState.value > 0) {
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
        Spacer(modifier = Modifier.height(60.dp))
        MinRemainAlarm(scrollState.value, states.minTime, states, calculateMinTime)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Transparent)
                    .padding(12.dp, 20.dp),
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                ),
                onDismissRequest = { isAlarmClick.value = null },
                onDoneClickListener = {
                    updateAlarm(isAlarmClick.value!!.copy())
                    isAlarmClick.value = null
                },
                meridiemState = meridiemState,
                hourState = hourState,
                minuteState = minuteState,
                selectedDayOfWeek = selectedDayOfWeek
            )
        }
        if (isShowTimePicker.value) {
            meridiemState.selectedItem = DEFAULT_MERIDIEM
            hourState.selectedItem = DEFAULT_HOUR
            minuteState.selectedItem = DEFAULT_MINUTE
            TimePickerDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Transparent)
                    .padding(12.dp, 20.dp),
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
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
                minute = DEFAULT_MINUTE.toInt(),
                selectedDayOfWeek = selectedDayOfWeek
            )
        }
    }
}


@Composable
fun MinRemainAlarm(
    scrollValue: Int,
    minTime: RemainTime?,
    states: AlarmContract.AlarmUiState,
    calculateMinTime: (RemainTime?) -> Unit
) {
    LaunchedEffect(minTime) {
        while (true) {
            if (states.alarmList.isNotEmpty()) {
                calculateMinTime(calculateRemainTime(states.alarmList))
                Log.d("MainActivity", "MinRemainAlarm: $minTime")
            }
            delay(1000)
        }
    }
    val alpha = 1f - (scrollValue.toFloat() / 350f).coerceIn(0f, 1f)

    if (minTime != null) {
        Text(
            text = "${minTime.remainHour}시간 ${minTime.remainMinute}분 후에",
            modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 0.dp),
            fontSize = 32.sp,
            color = Color.Black.copy(alpha)
        )

        Text(
            text = "알람이 울립니다.",
            fontSize = 32.sp,
            color = Color.Black.copy(alpha)
        )

        Text(
            text = "${minTime.month}월 ${minTime.day}일 ${minTime.meridiem} (${minTime.dayOfWeek}) ${
                stringResource(
                    R.string.alarm_time,
                    minTime.hour,
                    minTime.minute
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

fun checkTimeChange(minTime: AlarmStateEntity): Pair<Int, Int> {
    val localDateTime = LocalDateTime.now()
    var hour = minTime.hour - localDateTime.hour
    if (minTime.meridiem == getString(R.string.pm) && minTime.hour != 12) {
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

@OptIn(ExperimentalTime::class)
fun getNextDate(alarm: AlarmStateEntity): Calendar {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()

    // 현재 시간을 LocalDateTime으로 변환
    val currentDateTime = now.toLocalDateTime(timeZone)

    // 24시간 형식 변환 (기존 로직과 동일)
    val hour24 = if (alarm.meridiem == getString(R.string.pm)) {
        if (alarm.hour == 12) alarm.hour else alarm.hour + 12
    } else {
        if (alarm.hour == 12) 0 else alarm.hour
    }

    // 오늘의 알람 시간 생성
    val alarmTime = LocalTime(hour24, alarm.minute, 0)
    var alarmDateTime = currentDateTime.date.atTime(alarmTime)

    // 현재 시간과 비교하여 다음 실행 시간 결정
    if (now.epochSeconds * 1000 + now.nanosecondsOfSecond / 1_000_000 >=
        alarmDateTime.toInstant(timeZone).epochSeconds * 1000 +
        alarmDateTime.toInstant(timeZone).nanosecondsOfSecond / 1_000_000
    ) {
        alarmDateTime = alarmDateTime.date.plus(1, DateTimeUnit.DAY).atTime(alarmTime)
    }

    // Calendar로 변환 반환
    return Calendar.getInstance().apply {
        val instant = alarmDateTime.toInstant(timeZone)
        timeInMillis = instant.epochSeconds * 1000 + instant.nanosecondsOfSecond / 1_000_000
    }
}

fun calculateRemainTime(alarms: List<AlarmStateEntity>): RemainTime? {
    //Log.d("MainActivity", "alarms: $alarms")
    while (alarms.isNotEmpty()) {
        val remainTime = checkTimeChange(alarms.filter { it.isActive }
            .minByOrNull { getNextDate(it).timeInMillis }!!)
        val date = getNextDate(alarms.filter { it.isActive }
            .minByOrNull { getNextDate(it).timeInMillis }!!)

        return RemainTime(
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH),
            if (date.get(Calendar.AM_PM) == Calendar.AM) getString(R.string.am) else getString(
                R.string.pm
            ),
            getDayOfWeek(
                date.get(Calendar.DAY_OF_WEEK)
            ),
            date.get(Calendar.MINUTE),
            if (date.get(Calendar.HOUR_OF_DAY) == 0) 12 else date.get(
                Calendar.HOUR
            ),
            remainTime.first,
            remainTime.second
        )
    }

    return null
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