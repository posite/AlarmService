package com.posite.my_alarm.ui.alarm

import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.ui.base.UiEffect
import com.posite.my_alarm.ui.base.UiEvent
import com.posite.my_alarm.ui.base.UiState
import com.posite.my_alarm.util.Screen

class AlarmContract {
    sealed class AlarmEvent : UiEvent {
        data object GetAlarmList : AlarmEvent()
        data class GetAlarmState(val id: Long) : AlarmEvent()
        data class InsertAlarm(val alarm: AlarmStateEntity) : AlarmEvent()
        data class UpdateAlarm(val alarm: AlarmStateEntity) : AlarmEvent()
        data class DeleteAlarm(val alarm: AlarmStateEntity) : AlarmEvent()
        data class CalculateMinTime(val minTime: RemainTime?) : AlarmEvent()
    }

    data class AlarmUiState(
        val alarmList: List<AlarmStateEntity> = emptyList(),
        val selectedAlarm: AlarmStateEntity? = null,
        val minTime: RemainTime? = null
    ) : UiState

    sealed class AlarmEffect : UiEffect {
        data class ShowToast(val message: String) : AlarmEffect()
        data class ShowAlarmDetails(val alarm: AlarmStateEntity) : AlarmEffect()
        data object NavigateToAlarmList : AlarmEffect()
        data class ItemInserted(val isSuccess: Boolean, val alarm: AlarmStateEntity) : AlarmEffect()
        data class NavigateToScreen(val screen: Screen) : AlarmEffect()
    }
}