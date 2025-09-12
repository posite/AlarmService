package com.posite.my_alarm.ui.timer

import com.posite.my_alarm.ui.base.UiEffect
import com.posite.my_alarm.ui.base.UiEvent
import com.posite.my_alarm.ui.base.UiState

class TimerContract {
    sealed class TimerEvent : UiEvent {
        data object TikTok : TimerEvent()
        data class SetTimer(val timeInSeconds: Int) : TimerEvent()
        data object DeleteTimer : TimerEvent()
        data object PauseTimer : TimerEvent()
        data object RestartTimer : TimerEvent()
    }

    data class TimerUiState(
        val initialTime: Int = -1,
        val remainTime: Int = -1,
        val isRunning: Boolean = false
    ) : UiState

    sealed class TimerEffect : UiEffect {

    }

}