package com.posite.my_alarm.ui.main

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.alarm.Alarm
import com.posite.my_alarm.ui.picker.TimePickerDialog
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.util.AlarmReceiver
import com.posite.my_alarm.util.AlarmReceiver.Companion.TAG
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
        //enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
        askNotificationPermission()

        if (alertPermissionCheck(this)) {
            onObtainingPermissionOverlayWindow(this)
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
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDeleteMode.value) DELETE_MODE_TITLE else ALARM_MODE_TITLE,
                            fontSize = 20.sp,
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
                                        removeAlarm(
                                            Intent(
                                                context,
                                                AlarmReceiver::class.java
                                            ).let { intent ->
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
                                    set.clear()
                                    isDeleteMode.value = false
                                } else {
                                    isShowTimePicker.value = true
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.padding(8.dp),
                                tint = Color.Black,
                                contentDescription = "Add",
                                imageVector = if (isDeleteMode.value) Icons.Default.Delete else Icons.Default.Add
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.padding(12.dp, 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(viewModel.currentState.alarmList) { item ->
                            Alarm(alarm = item, isDeleteMode = isDeleteMode, onAlarmSelected = {
                                set.add(item)
                                Log.d("MainActivity", "set: $set")
                            }, onAlarmUnselected = {
                                set.remove(it)
                                Log.d("MainActivity", "set: $set")
                            }, onSwitchChanges = {
                                if (it) {
                                    addAlarm(
                                        item.meridiem,
                                        item.hour,
                                        item.minute.toString(),
                                        Intent(context, AlarmReceiver::class.java).putExtra(
                                            TAG,
                                            item.id
                                        )
                                            .let { intent ->
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
                                            TAG,
                                            item.id
                                        )
                                            .let { intent ->
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
                                Log.d("MainActivity", viewModel.currentState.alarmList.toString())
                            }, onAlarmClicked = {
                                isAlarmClick.value = item
                                Log.d("MainActivity", "isAlarmClick: ${isAlarmClick.value}")
                            })
                        }
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
                                        hour = hourState.selectedItem,
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
                            if (it.isSuccess.not() || viewModel.currentState.alarmList.isEmpty()) {
                                delay(1000)
                            }
                            val intent = Intent(this@MainActivity, AlarmReceiver::class.java)
                            val pendingIntent = PendingIntent.getBroadcast(
                                this@MainActivity,
                                viewModel.currentState.alarmList.last().id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            addAlarm(
                                viewModel.currentState.alarmList.last().meridiem,
                                viewModel.currentState.alarmList.last().hour,
                                viewModel.currentState.alarmList.last().minute.toString(),
                                pendingIntent
                            )
                        }

                        is MainContract.MainEffect.ItemUpdated -> {
                            if (it.isSuccess.not()) {
                                delay(1000)
                            }
                            val intent = Intent(this@MainActivity, AlarmReceiver::class.java)
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
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            if (meridiemState == "오후") {
                if (hourState == 12) {
                    set(Calendar.HOUR_OF_DAY, hourState)
                } else {
                    set(Calendar.HOUR_OF_DAY, hourState + 12)
                }
            } else {
                set(Calendar.HOUR_OF_DAY, hourState)
            }
            set(Calendar.MINUTE, minuteState.toInt())
            set(Calendar.SECOND, 0)
        }
        Log.d("MainActivity", "now: ${System.currentTimeMillis()} alarm: ${calendar.timeInMillis}")
        Toast.makeText(
            this,
            "${meridiemState} ${hourState}시 ${minuteState}분 알람이 설정되었습니다.",
            Toast.LENGTH_SHORT
        ).show()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis, intent
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(permission.POST_NOTIFICATIONS), 1000)
            }
        }
    }

    private fun onObtainingPermissionOverlayWindow(context: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.packageName)
        )
        context.startActivityForResult(intent, 0)
    }


    private fun alertPermissionCheck(context: Context?): Boolean {
        return !Settings.canDrawOverlays(context)
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
        private const val DELETE_MODE_TITLE = "삭제"
        private const val ALARM_MODE_TITLE = "알람"
    }
}

