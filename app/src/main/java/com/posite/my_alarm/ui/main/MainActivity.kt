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
        onEffect()
        setContent {
            val context = LocalContext.current
            val isShowTimePicker = remember { mutableStateOf(false) }
            val meridiemState = remember { PickerState("오전") }
            val hourState = remember { PickerState(0) }
            val minuteState = remember { PickerState("00") }
            val isDeleteMode = remember { mutableStateOf(false) }
            val set = mutableSetOf<AlarmStateEntity>()
            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            MyAlarmTheme {
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
                            text = if (isDeleteMode.value) "삭제" else "알람",
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
                                                    PendingIntent.FLAG_IMMUTABLE
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
                    LazyColumn(modifier = Modifier.padding(12.dp, 0.dp)) {
                        items(viewModel.currentState.alarmList) { item ->
                            Alarm(alarm = item, isDeleteMode = isDeleteMode, onAlarmSelected = {
                                set.add(item)
                                Log.d("MainActivity", "set: $set")
                            }, onAlarmUnselected = {
                                set.remove(it)
                                Log.d("MainActivity", "set: $set")
                            }, onSwitchChanges = {
                                if (it.not()) {
                                    removeAlarm(
                                        Intent(context, AlarmReceiver::class.java).putExtra(TAG, 0)
                                            .let { intent ->
                                                PendingIntent.getBroadcast(
                                                    context,
                                                    item.id.toInt(),
                                                    intent,
                                                    PendingIntent.FLAG_IMMUTABLE
                                                )
                                            }
                                    )
                                }
                                viewModel.updateAlarmState(item.copy(isActive = item.isActive.not()))
                                Log.d("MainActivity", viewModel.currentState.alarmList.toString())
                            })
                        }
                    }
                    if (isShowTimePicker.value) {
                        TimePickerDialog(
                            modifier = Modifier.background(color = Color.White),
                            properties = DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                            ),
                            onDismissRequest = { isShowTimePicker.value = false },
                            onDoneClickListener = {
                                isShowTimePicker.value = false
                                Toast.makeText(
                                    context,
                                    "${meridiemState.selectedItem} ${hourState.selectedItem}시 ${minuteState.selectedItem}분",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                            minuteState = minuteState
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
                                PendingIntent.FLAG_IMMUTABLE
                            )
                            addAlarm(
                                viewModel.currentState.alarmList.last().meridiem,
                                viewModel.currentState.alarmList.last().hour,
                                viewModel.currentState.alarmList.last().minute.toString(),
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
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis, intent
        )
        /*alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intent
        )*/
    }

    private fun removeAlarm(intent: PendingIntent) {
        alarmManager.cancel(intent)
        intent.cancel()
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


    fun alertPermissionCheck(context: Context?): Boolean {
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
}

