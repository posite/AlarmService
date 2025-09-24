package com.posite.my_alarm.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.alarm.AlarmContract
import com.posite.my_alarm.ui.alarm.AlarmScreen
import com.posite.my_alarm.ui.alarm.RemainTime
import com.posite.my_alarm.ui.picker.DayOfWeek
import com.posite.my_alarm.ui.timer.TimerContract
import com.posite.my_alarm.ui.timer.TimerScreen
import com.posite.my_alarm.util.Screen

@Composable
fun AppNavigation(
    navController: NavHostController, isDeleteMode: MutableState<Boolean>,
    isAlarmClick: MutableState<AlarmStateEntity?>,
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    alarmStates: AlarmContract.AlarmUiState,
    timerStates: TimerContract.TimerUiState,
    selectedDayOfWeek: MutableSet<DayOfWeek>,
    onSwitchChanges: (Boolean, AlarmStateEntity) -> Unit,
    deleteAlarm: (AlarmStateEntity) -> Unit,
    updateAlarm: (AlarmStateEntity) -> Unit,
    insertAlarm: () -> Unit,
    calculateMinTime: (RemainTime?) -> Unit,
    onTimerSet: (Int) -> Unit,
    onTimerTikTok: () -> Unit,
    onTimerDelete: () -> Unit,
    onTimerPause: () -> Unit,
    onTimerRestart: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = Screen.AlarmScreen.route
    ) {
        composable(Screen.AlarmScreen.route) {
            AlarmScreen(
                isDeleteMode,
                isAlarmClick,
                meridiemState,
                hourState,
                minuteState,
                selectedDayOfWeek,
                alarmStates,
                onSwitchChanges,
                deleteAlarm,
                updateAlarm,
                insertAlarm,
                calculateMinTime
            )
        }

        composable(Screen.TimerScreen.route) {
            TimerScreen(
                timerStates,
                onTimerSet,
                onTimerTikTok,
                onTimerDelete,
                onTimerPause,
                onTimerRestart
            )
        }
    }
}