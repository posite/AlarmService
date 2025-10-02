package com.posite.my_alarm.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.alarm.AlarmContract
import com.posite.my_alarm.ui.alarm.AlarmViewModel
import com.posite.my_alarm.ui.alarm.getNextOccurrences
import com.posite.my_alarm.ui.nav.AppNavigation
import com.posite.my_alarm.ui.nav.BottomNavigationBar
import com.posite.my_alarm.ui.picker.DayOfWeek
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.ui.timer.TimerViewModel
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
import java.lang.String.format
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val alarmVM by viewModels<AlarmViewModel>()
    private val timerVM by viewModels<TimerViewModel>()
    private lateinit var navController: NavHostController

    @Inject
    lateinit var alarmManager: AlarmManager

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermission(this, alarmManager)
        alarmVM.getAlarmList()
        //this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        onEffect()
        setContent {
            val meridiemState = remember { PickerState(DEFAULT_MERIDIEM) }
            val hourState = remember { PickerState(DEFAULT_HOUR) }
            val minuteState = remember { PickerState(DEFAULT_MINUTE) }
            val isDeleteMode = remember { mutableStateOf(DEFAULT_MODE_STATE) }
            val isAlarmClick = remember { mutableStateOf<AlarmStateEntity?>(null) }
            val selectedDayOfWeek = remember { mutableSetOf<DayOfWeek>() }
            navController = rememberNavController()
            MyAlarmTheme {
                BackHandler {
                    if (isDeleteMode.value) {
                        isDeleteMode.value = false
                    } else {
                        finish()
                    }
                }

                Scaffold(bottomBar = { BottomNavigationBar(navController) }) {
                    AppNavigation(
                        navController, isDeleteMode,
                        isAlarmClick,
                        meridiemState,
                        hourState,
                        minuteState,
                        alarmVM.currentState,
                        timerVM.currentState,
                        selectedDayOfWeek,
                        onSwitchChanges = { isActive, alarm ->
                            if (isActive) {
                                addAlarm(alarm)
                                Toast.makeText(
                                    this,
                                    format(
                                        getString(R.string.add_alarm_toast),
                                        meridiemState.selectedItem,
                                        hourState.selectedItem,
                                        minuteState.selectedItem.toInt()
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                removeAlarm(alarm)
                            }
                            alarmVM.updateAlarmState(alarm.copy(isActive = isActive))
                            Log.d(
                                "MainActivity",
                                alarmVM.currentState.alarmList.toString()
                            )
                        },
                        deleteAlarm = { alarm ->
                            removeAlarm(alarm, DELETE_ALARM)
                            alarmVM.deleteAlarmState(alarm)
                        },
                        updateAlarm = { alarm ->
                            //if(selectedDayOfWeek.isNotEmpty()) {}
                            if (alarm.isActive) {
                                updateAlarm(
                                    alarm
                                )
                            }

                            Toast.makeText(
                                this,
                                format(
                                    getString(R.string.update_alarm_toast),
                                    meridiemState.selectedItem,
                                    hourState.selectedItem,
                                    minuteState.selectedItem.toInt()
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                            alarmVM.updateAlarmState(
                                AlarmStateEntity(
                                    id = alarm.id,
                                    hour = hourState.selectedItem,
                                    minute = minuteState.selectedItem.toInt(),
                                    meridiem = meridiemState.selectedItem,
                                    isActive = alarm.isActive,
                                    dayOfWeeks = selectedDayOfWeek.toMutableList()
                                )
                            )
                            selectedDayOfWeek.clear()
                        },
                        insertAlarm = {
                            //if(selectedDayOfWeek.isNotEmpty()) {}
                            alarmVM.insertAlarmState(
                                AlarmStateEntity(
                                    hour = if (meridiemState.selectedItem == this.getString(R.string.am) && hourState.selectedItem == 12) 0 else hourState.selectedItem,
                                    minute = minuteState.selectedItem.toInt(),
                                    meridiem = meridiemState.selectedItem,
                                    isActive = true,
                                    dayOfWeeks = selectedDayOfWeek.toMutableList()
                                )
                            )
                            selectedDayOfWeek.clear()
                        },
                        calculateMinTime = { minTime ->
                            alarmVM.saveRemainTime(minTime)
                        },
                        onTimerSet = { time ->
                            timerVM.setTimer(time)
                        },
                        onTimerTikTok = {
                            timerVM.tikTok()
                        },
                        onTimerDelete = {
                            timerVM.deleteTimer()
                        },
                        onTimerPause = {
                            timerVM.pauseTimer()
                        },
                        onTimerRestart = {
                            timerVM.restartTimer()
                        }
                    )
                }
            }
        }
    }

    private fun requestPermission(activity: ComponentActivity, alarmManager: AlarmManager) {
        if (alarmManager.canScheduleExactAlarms().not()) {
            ExactAlarmPermission(activity).onSuccess { }.onDeny { permissions ->
                Log.d("MainActivity", "onDeny: $permissions")
            }.request()

        }
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            NotificationPermission(activity).onSuccess { }.onDeny { permissions ->
                Log.d("MainActivity", "onDeny: $permissions")
            }.request()
        }
        if (Settings.canDrawOverlays(activity).not()) {
            OverlayPermission(activity).onSuccess { }.onDeny { permissions ->
                Log.d("MainActivity", "onDeny: $permissions")
            }.request()
        }
    }


    private fun onEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmVM.effect.collect {
                    when (it) {
                        is AlarmContract.AlarmEffect.ItemInserted -> {
                            delay(1000)

                            Log.d("added", alarmVM.currentState.alarmList.last().id.toString())
                            if (it.alarm.dayOfWeeks.isEmpty()) {
                                it.alarm.dayOfWeeks.addAll(DayOfWeek.entries)
                            }
                            addAlarm(it.alarm)
                            Toast.makeText(
                                this@MainActivity,
                                format(
                                    getString(R.string.add_alarm_toast),
                                    it.alarm.meridiem,
                                    it.alarm.hour,
                                    it.alarm.minute
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is AlarmContract.AlarmEffect.NavigateToScreen -> {
                            navController.navigate(it.screen.route)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun createAlarmIntent(context: Context, it: AlarmStateEntity, date: DayOfWeek): Intent =
        Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, it.id)
            .putExtra(ALARM_MERIDIEM, it.meridiem)
            .putExtra(ALARM_HOUR, it.hour)
            .putExtra(ALARM_MINUTE, it.minute)
            .putExtra(
                VERSION_CODE,
                packageManager.getPackageInfo(packageName, 0).longVersionCode
            )
            .putExtra(DAY_OF_WEEKS, date.ordinal)

    @SuppressLint("ScheduleExactAlarm")
    private fun addAlarm(alarm: AlarmStateEntity) {
        //Log.d("intent", intent.hashCode().toString())
        //Log.d("add", "id: $id")
        val baseCalendar = getNextOccurrences(
            alarm.hour,
            alarm.minute,
            alarm.dayOfWeeks
        )
        for (date in alarm.dayOfWeeks) {
            val pendingIntent = createAlarmIntent(
                this@MainActivity,
                alarm,
                date
            ).let { intent ->
                PendingIntent.getBroadcast(
                    this@MainActivity,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
            val alarmMills = baseCalendar[date]!!

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmMills, pendingIntent
            )
        }
    }

    private fun removeAlarm(
        alarm: AlarmStateEntity,
        isDeleted: Boolean = CANCEL_ALARM
    ) {
        Log.d("remove", "removeAlarm: ${alarm.id} ${alarm.hour} ${alarm.minute} ${alarm.meridiem}")
        for (date in alarm.dayOfWeeks) {
            val pendingIntent = createAlarmIntent(
                this@MainActivity,
                alarm,
                date
            ).let { intent ->
                PendingIntent.getBroadcast(
                    this@MainActivity,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
            alarmManager.cancel(pendingIntent)
        }
        Log.d("remove", "id: ${alarm.id}")
        Log.d("intent", intent.hashCode().toString())
        if (isDeleted) {
            Toast.makeText(
                this,
                format(
                    getString(R.string.delete_alarm_toast),
                    alarm.meridiem,
                    alarm.hour,
                    alarm.minute
                ),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                format(
                    getString(R.string.cancel_alarm_toast),
                    alarm.meridiem,
                    alarm.hour,
                    alarm.minute
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
        //Log.d("compare", added!!.filterEquals(removed).toString())
    }

    private fun updateAlarm(alarm: AlarmStateEntity) {
        removeAlarm(alarm)
        addAlarm(alarm)
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
            alarmManager.cancelAll()
        }
    }

    companion object {
        const val VERSION_CODE = "CODE"
        const val DEFAULT_MERIDIEM = "오전"
        const val DEFAULT_HOUR = 6
        const val DEFAULT_HOUR_STRING = "00"
        const val DEFAULT_MINUTE = "00"
        const val DEFAULT_SECOND = "00"
        const val DEFAULT_MODE_STATE = false
        const val ALARM_MODE_TITLE = "알람"
        private const val UPDATE_ALARM = true
        private const val NOT_UPDATE = false
        private const val DELETE_ALARM = true
        private const val CANCEL_ALARM = false
        const val DAY_OF_WEEKS = "DAYS"
    }
}