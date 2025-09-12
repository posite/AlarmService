package com.posite.my_alarm.ui.timer

import android.util.Log
import com.posite.my_alarm.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor() :
    BaseViewModel<TimerContract.TimerEvent, TimerContract.TimerUiState, TimerContract.TimerEffect>() {
    override fun createInitialState(): TimerContract.TimerUiState {
        return TimerContract.TimerUiState()
    }

    override fun handleEvent(event: TimerContract.TimerEvent) {
        when (event) {
            is TimerContract.TimerEvent.TikTok -> {
                val currentTime = currentState.remainTime
                if (currentTime > 0) {
                    setState { copy(remainTime = currentTime - 1, isRunning = true) }
                } else {
                    setState { copy(remainTime = 0, isRunning = false) }
                }
            }

            is TimerContract.TimerEvent.SetTimer -> {
                if (event.timeInSeconds > 0) {
                    setState {
                        copy(
                            initialTime = event.timeInSeconds,
                            remainTime = event.timeInSeconds,
                            isRunning = true
                        )
                    }
                } else {
                    setState { copy(initialTime = 0, remainTime = 0, isRunning = false) }
                }
            }

            is TimerContract.TimerEvent.DeleteTimer -> {
                setState { copy(initialTime = 0, remainTime = 0, isRunning = false) }
            }

            is TimerContract.TimerEvent.PauseTimer -> {
                setState { copy(isRunning = false) }
                Log.d("TimerViewModel - PauseTimer", "pause")
            }

            is TimerContract.TimerEvent.RestartTimer -> {
                if (currentState.remainTime > 0) {
                    setState { copy(isRunning = true) }
                    Log.d("TimerViewModel - RestartTimer", "restart")
                }
            }
        }
    }

    fun setTimer(timeInSeconds: Int) {
        setEvent(TimerContract.TimerEvent.SetTimer(timeInSeconds))
    }

    fun tikTok() {
        setEvent(TimerContract.TimerEvent.TikTok)
    }

    fun deleteTimer() {
        setEvent(TimerContract.TimerEvent.DeleteTimer)
    }

    fun pauseTimer() {
        setEvent(TimerContract.TimerEvent.PauseTimer)
    }

    fun restartTimer() {
        setEvent(TimerContract.TimerEvent.RestartTimer)
    }

}