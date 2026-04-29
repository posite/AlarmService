package com.posite.my_alarm.ui.alarm

import android.util.Log
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.repository.TimeRepository
import com.posite.my_alarm.ui.base.BaseViewModel
import com.posite.my_alarm.util.onAsyncIO
import com.posite.my_alarm.util.onIO
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(private val repository: TimeRepository) :
    BaseViewModel<AlarmContract.AlarmEvent, AlarmContract.AlarmUiState, AlarmContract.AlarmEffect>() {

    override fun createInitialState(): AlarmContract.AlarmUiState {
        return AlarmContract.AlarmUiState()
    }

    override fun handleEvent(event: AlarmContract.AlarmEvent) {
        when (event) {
            is AlarmContract.AlarmEvent.GetAlarmList -> {
                onIO {
                    repository.getAlarmStates().collect { alarms ->
                        Log.d("AlarmViewModel", "Alarms: $alarms")
                        setState { copy(alarmList = alarms) }
                        //setState { copy(minTime = calculateRemainTime(alarms)) }
                    }
                }
            }

            is AlarmContract.AlarmEvent.GetAlarmState -> {
                onIO {
                    repository.getAlarmStateById(id = event.id).collect { result ->
                        result?.let { setState { copy(selectedAlarm = it) } }
                    }
                }
            }

            is AlarmContract.AlarmEvent.InsertAlarm -> {
                onIO {
                    onAsyncIO {
                        repository.addAlarm(alarm = event.alarm)
                    }.await()
                    setEffect {
                        AlarmContract.AlarmEffect.ItemInserted(true, event.alarm)
                    }
                }

            }

            is AlarmContract.AlarmEvent.UpdateAlarm -> {
                onIO {
                    repository.updateAlarm(alarm = event.alarm)
                }
            }

            is AlarmContract.AlarmEvent.DeleteAlarm -> {
                onIO {
                    repository.deleteAlarms(alarm = event.alarm)
                }
            }

            is AlarmContract.AlarmEvent.CalculateMinTime -> {
                if (event.minTime != null) {
                    setState { copy(minTime = event.minTime) }
                } else {
                    setState { copy(minTime = null) }
                }
            }
        }
    }


    fun getAlarmList() = setEvent(AlarmContract.AlarmEvent.GetAlarmList)

    fun getAlarmState(id: Long) = setEvent(AlarmContract.AlarmEvent.GetAlarmState(id))

    fun insertAlarmState(alarm: AlarmStateEntity) =
        setEvent(AlarmContract.AlarmEvent.InsertAlarm(alarm))

    fun updateAlarmState(alarm: AlarmStateEntity) =
        setEvent(AlarmContract.AlarmEvent.UpdateAlarm(alarm))

    fun deleteAlarmState(alarm: AlarmStateEntity) =
        setEvent(AlarmContract.AlarmEvent.DeleteAlarm(alarm))

    fun saveRemainTime(minTime: RemainTime?) =
        setEvent(AlarmContract.AlarmEvent.CalculateMinTime(minTime))

}