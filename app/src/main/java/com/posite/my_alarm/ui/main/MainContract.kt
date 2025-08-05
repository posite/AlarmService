package com.posite.my_alarm.ui.main

import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.ui.base.UiEffect
import com.posite.my_alarm.ui.base.UiEvent
import com.posite.my_alarm.ui.base.UiState

class MainContract {
    sealed class MainEvent : UiEvent {
        data object GetAlarmList : MainEvent()
        data class GetAlarmState(val id: Long) : MainEvent()
        data class InsertAlarm(val alarm: AlarmStateEntity) : MainEvent()
        data class UpdateAlarm(val alarm: AlarmStateEntity) : MainEvent()
        data class DeleteAlarm(val alarm: AlarmStateEntity) : MainEvent()
        data class ChangeAlarmActivation(val alarm: AlarmStateEntity) : MainEvent()
    }

    data class MainUiState(
        val alarmList: List<AlarmStateEntity> = emptyList(),
        val selectedAlarm: AlarmStateEntity? = null,
    ) : UiState

    sealed class MainEffect : UiEffect {
        data class ShowToast(val message: String) : MainEffect()
        data class ShowAlarmDetails(val alarm: AlarmStateEntity) : MainEffect()
        data object NavigateToAlarmList : MainEffect()
        data class ItemInserted(val isSuccess: Boolean, val alarm: AlarmStateEntity) : MainEffect()
        data class ItemUpdated(val isActivated: Boolean) : MainEffect()
    }
}