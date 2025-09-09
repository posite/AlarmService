package com.posite.my_alarm.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.alarm.AlarmScreen
import com.posite.my_alarm.ui.alarm.RemainTime
import com.posite.my_alarm.ui.main.MainContract
import com.posite.my_alarm.util.Screen

@Composable
fun AppNavigation(
    navController: NavHostController, isDeleteMode: MutableState<Boolean>,
    isAlarmClick: MutableState<AlarmStateEntity?>,
    meridiemState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<String>,
    states: MainContract.MainUiState,
    onSwitchChanges: (Boolean, AlarmStateEntity) -> Unit,
    deleteAlarm: (AlarmStateEntity) -> Unit,
    updateAlarm: (AlarmStateEntity) -> Unit,
    insertAlarm: () -> Unit,
    calculateMinTime: (RemainTime?) -> Unit
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
                states,
                onSwitchChanges,
                deleteAlarm,
                updateAlarm,
                insertAlarm,
                calculateMinTime
            )
        }

        composable(Screen.TimerScreen.route) {
            Screen.TimerScreen
        }
    }
}