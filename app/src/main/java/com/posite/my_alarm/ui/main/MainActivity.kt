package com.posite.my_alarm.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.picker.TimePickerDialog
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.util.AlarmReceiver
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_HOUR
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_ID
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MERIDIEM
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MINUTE
import com.posite.my_alarm.util.permission.ExactAlarmPermission
import com.posite.my_alarm.util.permission.NotificationPermission
import com.posite.my_alarm.util.permission.OverlayPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val viewModel by viewModels<MainViewModel>()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (alarmManager.canScheduleExactAlarms().not()) {
                ExactAlarmPermission(this).onSuccess { }.onDeny { permissions ->
                    Log.d("MainActivity", "onDeny: $permissions")
                }.request()

            }
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                NotificationPermission(this).onSuccess { }.onDeny { permissions ->
                    Log.d("MainActivity", "onDeny: $permissions")
                }.request()
            }
            if (Settings.canDrawOverlays(this).not()) {
                OverlayPermission(this).onSuccess { }.onDeny { permissions ->
                    Log.d("MainActivity", "onDeny: $permissions")
                }.request()
            }
        }

        viewModel.getAlarmList()
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        onEffect()
        setContent {
            val context = LocalContext.current
            val isShowTimePicker = remember { mutableStateOf(DEFAULT_MODE_STATE) }
            val meridiemState = remember { PickerState(DEFAULT_MERIDIEM) }
            val hourState = remember { PickerState(DEFAULT_HOUR) }
            val minuteState = remember { PickerState(DEFAULT_MINUTE) }
            val isDeleteMode = remember { mutableStateOf(DEFAULT_MODE_STATE) }
            val set = mutableSetOf<AlarmStateEntity>()
            val isAlarmClick = remember { mutableStateOf<AlarmStateEntity?>(null) }
            var minTime by remember { mutableStateOf<AlarmStateEntity?>(null) }
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
            MyAlarmTheme {
                BackHandler {
                    if (isDeleteMode.value) {
                        isDeleteMode.value = false
                    } else {
                        finish()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LaunchedEffect(viewModel.currentState) {
                        if (viewModel.currentState.alarmList.isEmpty().not()) {
                            minTime = viewModel.currentState.alarmList.filter { it.isActive }
                                .minByOrNull { getNextDate(it).timeInMillis }
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp))
                    MinRemainAlarm(scrollState, minTime)

                    Spacer(modifier = Modifier.height(24.dp))
                    AlarmList(viewModel.currentState.alarmList,
                        isDeleteMode,
                        set,
                        isShowTimePicker,
                        nestedScrollConnection,
                        isAlarmClick,
                        onSwitchChanges = { isActive, item ->
                            if (isActive) {
                                addAlarm(
                                    item.meridiem,
                                    item.hour,
                                    item.minute.toString(),
                                    Intent(context, AlarmReceiver::class.java).putExtra(
                                        ALARM_ID,
                                        item.id
                                    ).putExtra(ALARM_MERIDIEM, item.meridiem)
                                        .putExtra(ALARM_HOUR, item.hour)
                                        .putExtra(ALARM_MINUTE, item.minute).let { intent ->
                                            PendingIntent.getBroadcast(
                                                context,
                                                item.id.toInt(),
                                                intent,
                                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                            )
                                        }
                                )
                            } else {
                                removeAlarm(
                                    Intent(context, AlarmReceiver::class.java).putExtra(
                                        ALARM_ID,
                                        item.id
                                    ).let { intent ->
                                        PendingIntent.getBroadcast(
                                            context,
                                            item.id.toInt(),
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )
                                    }
                                )
                            }
                            viewModel.updateAlarmState(item.copy(isActive = item.isActive.not()))
                            Log.d(
                                "MainActivity",
                                viewModel.currentState.alarmList.toString()
                            )
                        }
                    ) { alarm ->
                        removeAlarm(
                            Intent(context, AlarmReceiver::class.java).let { intent ->
                                PendingIntent.getBroadcast(
                                    context,
                                    alarm.id.toInt(),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                            }
                        )
                        viewModel.deleteAlarmState(alarm)
                    }

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
                                viewModel.updateAlarmState(
                                    AlarmStateEntity(
                                        id = isAlarmClick.value!!.id,
                                        hour = hourState.selectedItem,
                                        minute = minuteState.selectedItem.toInt(),
                                        meridiem = meridiemState.selectedItem,
                                        isActive = true
                                    )
                                )
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
                                viewModel.insertAlarmState(
                                    AlarmStateEntity(
                                        hour = if (meridiemState.selectedItem == "오전" && hourState.selectedItem == 12) 0 else hourState.selectedItem,
                                        minute = minuteState.selectedItem.toInt(),
                                        meridiem = meridiemState.selectedItem,
                                        isActive = true
                                    )
                                )
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
        }
    }

    private fun onEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect {
                    when (it) {
                        is MainContract.MainEffect.ItemInserted -> {
                            delay(1000)
                            val intent =
                                Intent(this@MainActivity, AlarmReceiver::class.java).putExtra(
                                    ALARM_ID,
                                    it.alarm.id
                                ).putExtra(
                                    ALARM_MERIDIEM,
                                    it.alarm.meridiem
                                )
                                    .putExtra(
                                        ALARM_HOUR,
                                        it.alarm.hour
                                    )
                                    .putExtra(
                                        ALARM_MINUTE,
                                        it.alarm.minute
                                    )
                            val pendingIntent = PendingIntent.getBroadcast(
                                this@MainActivity,
                                it.alarm.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            addAlarm(
                                it.alarm.meridiem,
                                it.alarm.hour,
                                it.alarm.minute.toString(),
                                pendingIntent
                            )
                        }

                        is MainContract.MainEffect.ItemUpdated -> {
                            delay(1000)
                            val intent =
                                Intent(this@MainActivity, AlarmReceiver::class.java).putExtra(
                                    ALARM_ID,
                                    viewModel.currentState.selectedAlarm!!.id
                                ).putExtra(
                                    ALARM_MERIDIEM,
                                    viewModel.currentState.selectedAlarm!!.meridiem
                                )
                                    .putExtra(
                                        ALARM_HOUR,
                                        viewModel.currentState.selectedAlarm!!.hour
                                    )
                                    .putExtra(
                                        ALARM_MINUTE,
                                        viewModel.currentState.selectedAlarm!!.minute
                                    )
                            val pendingIntent = PendingIntent.getBroadcast(
                                this@MainActivity,
                                viewModel.currentState.selectedAlarm!!.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            updateAlarm(
                                viewModel.currentState.selectedAlarm!!.meridiem,
                                viewModel.currentState.selectedAlarm!!.hour,
                                viewModel.currentState.selectedAlarm!!.minute.toString(),
                                pendingIntent
                            )
                        }

                        else -> {}
                    }
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
        val alarmMills = getNextDate(
            AlarmStateEntity(
                hour = hourState,
                minute = minuteState.toInt(),
                meridiem = meridiemState,
                isActive = true
            )
        ).timeInMillis

        Log.d("MainActivity", "now: ${System.currentTimeMillis()} alarm: $alarmMills")
        Toast.makeText(
            this,
            "$meridiemState ${hourState}시 ${minuteState}분 알람이 설정되었습니다.",
            Toast.LENGTH_SHORT
        ).show()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmMills, intent
        )
    }

    private fun removeAlarm(intent: PendingIntent) {
        alarmManager.cancel(intent)
    }

    private fun updateAlarm(
        meridiemState: String,
        hourState: Int,
        minuteState: String,
        intent: PendingIntent
    ) {
        removeAlarm(intent)
        addAlarm(meridiemState, hourState, minuteState, intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        val deniedPermissions = mutableListOf<String>()
        if (requestCode == 1000) {
            for ((index, result) in grantResults.withIndex()) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(permissions[index])
                }
            }
        }

        if (deniedPermissions.isNotEmpty()) {
            Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            requestPermissions(deniedPermissions.toTypedArray(), 1000)
        } else {
            Log.d("MainActivity", "onRequestPermissionsResult: $permissions")
        }
    }

    companion object {
        private const val DEFAULT_MERIDIEM = "오전"
        private const val DEFAULT_HOUR = 6
        private const val DEFAULT_MINUTE = "00"
        private const val DEFAULT_MODE_STATE = false
        const val DELETE_MODE_TITLE = "삭제"
        const val ALARM_MODE_TITLE = "알람"
    }
}