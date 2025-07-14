package com.posite.my_alarm.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.posite.my_alarm.R
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
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
import java.lang.String.format
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
        requestPermission(this, alarmManager)
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
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                        )
                                    }
                            )
                        } else {
                            removeAlarm(
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
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                        )
                                    }, item
                            )
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
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                            )
                        }, alarm, DELETE_ALARM)
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
                                hour = if (meridiemState.selectedItem == this.getString(R.string.am) && hourState.selectedItem == 12) 0 else hourState.selectedItem,
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

    private fun requestPermission(activity: ComponentActivity, alarmManager: AlarmManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
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
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
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
        intent: PendingIntent,
        isUpdated: Boolean = NOT_UPDATE
    ) {
        val alarmMills = getNextDate(
            AlarmStateEntity(
                hour = hourState,
                minute = minuteState.toInt(),
                meridiem = meridiemState,
                isActive = true,
            ),
            this
        ).timeInMillis

        Log.d("MainActivity", "now: ${System.currentTimeMillis()} alarm: $alarmMills")
        if (isUpdated) {
            Toast.makeText(
                this,
                format(
                    getString(R.string.update_alarm_toast),
                    meridiemState,
                    hourState,
                    minuteState.toInt()
                ),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                format(
                    getString(R.string.add_alarm_toast),
                    meridiemState,
                    hourState,
                    minuteState.toInt()
                ),
                Toast.LENGTH_SHORT
            ).show()
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmMills, intent
        )
    }

    private fun removeAlarm(
        intent: PendingIntent,
        alarm: AlarmStateEntity,
        isDeleted: Boolean = CANCEL_ALARM
    ) {
        alarmManager.cancel(intent)
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
        addAlarm(meridiemState, hourState, minuteState, intent, UPDATE_ALARM)
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
        private const val UPDATE_ALARM = true
        private const val NOT_UPDATE = false
        private const val DELETE_ALARM = true
        private const val CANCEL_ALARM = false
    }
}