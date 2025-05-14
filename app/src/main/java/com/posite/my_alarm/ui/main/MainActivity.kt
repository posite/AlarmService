package com.posite.my_alarm.ui.main

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.util.AlarmReceiver
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_HOUR
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_ID
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MERIDIEM
import com.posite.my_alarm.util.AlarmReceiver.Companion.ALARM_MINUTE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var alarmManager: AlarmManager

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.getAlarmList()
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        onEffect()
        setContent {
            val context = LocalContext.current
            val meridiemState = remember { PickerState(DEFAULT_MERIDIEM) }
            val hourState = remember { PickerState(DEFAULT_HOUR) }
            val minuteState = remember { PickerState(DEFAULT_MINUTE) }
            val isDeleteMode = remember { mutableStateOf(DEFAULT_MODE_STATE) }
            val isAlarmClick = remember { mutableStateOf<AlarmStateEntity?>(null) }
            MyAlarmTheme {
                BackHandler {
                    if (isDeleteMode.value) {
                        isDeleteMode.value = false
                    } else {
                        finish()
                    }
                }

                MainView(
                    isDeleteMode,
                    isAlarmClick,
                    meridiemState,
                    hourState,
                    minuteState,
                    alarmManager,
                    this,
                    viewModel.currentState,
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
                            removeAlarm(Intent(context, AlarmReceiver::class.java).putExtra(
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
                                })
                        }
                        viewModel.updateAlarmState(item.copy(isActive = item.isActive.not()))
                        Log.d(
                            "MainActivity",
                            viewModel.currentState.alarmList.toString()
                        )
                    },
                    deleteAlarm = { alarm ->
                        removeAlarm(Intent(context, AlarmReceiver::class.java).let { intent ->
                            PendingIntent.getBroadcast(
                                context,
                                alarm.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        })
                        viewModel.deleteAlarmState(alarm)
                    },
                    updateAlarm = {
                        viewModel.updateAlarmState(
                            AlarmStateEntity(
                                id = isAlarmClick.value!!.id,
                                hour = hourState.selectedItem,
                                minute = minuteState.selectedItem.toInt(),
                                meridiem = meridiemState.selectedItem,
                                isActive = true
                            )
                        )
                    },
                    insertAlarm = {
                        viewModel.insertAlarmState(
                            AlarmStateEntity(
                                hour = if (meridiemState.selectedItem == "오전" && hourState.selectedItem == 12) 0 else hourState.selectedItem,
                                minute = minuteState.selectedItem.toInt(),
                                meridiem = meridiemState.selectedItem,
                                isActive = true
                            )
                        )
                    }
                )
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

    @SuppressLint("ScheduleExactAlarm")
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
        const val DEFAULT_MERIDIEM = "오전"
        const val DEFAULT_HOUR = 6
        const val DEFAULT_MINUTE = "00"
        const val DEFAULT_MODE_STATE = false
        const val ALARM_MODE_TITLE = "알람"
    }
}