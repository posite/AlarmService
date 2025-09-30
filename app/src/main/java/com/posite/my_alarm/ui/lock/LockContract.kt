package com.posite.my_alarm.ui.lock

import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.ui.base.UiEffect
import com.posite.my_alarm.ui.base.UiEvent
import com.posite.my_alarm.ui.base.UiState

class LockContract {
    sealed class LockEvent : UiEvent {
        data class GetAlarmState(val id: Long) : LockEvent()
    }

    data class LockUiState(
        val alarm: AlarmStateEntity? = null
    ) : UiState

    sealed class LockEffect : UiEffect {
        data object ISAlarmNotExist : LockEffect()
        data class IsAlarmExist(val alarm: AlarmStateEntity) : LockEffect()
    }
}