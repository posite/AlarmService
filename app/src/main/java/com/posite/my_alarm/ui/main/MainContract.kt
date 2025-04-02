package com.posite.my_alarm.ui.main

import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.ui.base.UiEffect
import com.posite.my_alarm.ui.base.UiEvent
import com.posite.my_alarm.ui.base.UiState

class MainContract {
    sealed class MainEvent : UiEvent {
        data object GetAlarmList : MainEvent()
        data class GetAlarmState(val id: Long) : MainEvent()
        data class InsertAlarmState(val alarm: AlarmStateEntity) : MainEvent()
        data class UpdateAlarmState(val alarm: AlarmStateEntity) : MainEvent()
        data class DeleteAlarmState(val alarm: AlarmStateEntity) : MainEvent()
    }

    sealed class MainState {
        data class AlarmList(val alarmList: List<AlarmStateEntity>) : MainState()
        data class SelectedAlarm(val alarm: AlarmStateEntity) : MainState()
    }

    data class MainUiState(
        val alarmList: List<AlarmStateEntity> = emptyList(),
        val selectedAlarm: AlarmStateEntity? = null
    ) : UiState

    sealed class MainEffect : UiEffect {
        data class ShowToast(val message: String) : MainEffect()
        data class ShowAlarmDetails(val alarm: AlarmStateEntity) : MainEffect()
        data object NavigateToAlarmList : MainEffect()
    }
}