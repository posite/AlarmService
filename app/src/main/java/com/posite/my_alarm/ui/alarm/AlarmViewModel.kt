package com.posite.my_alarm.ui.alarm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.repository.TimeRepository
import com.posite.my_alarm.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
                viewModelScope.launch {
                    repository.getAlarmStates().collect { alarms ->
                        Log.d("AlarmViewModel", "Alarms: $alarms")
                        setState { copy(alarmList = alarms) }
                        setState { copy(minTime = calculateRemainTime(alarms)) }
                    }
                }
            }

            is AlarmContract.AlarmEvent.GetAlarmState -> {
                viewModelScope.launch {
                    repository.getAlarmStateById(id = event.id).collect { result ->
                        result?.let { setState { copy(selectedAlarm = it) } }
                    }
                }
            }

            is AlarmContract.AlarmEvent.InsertAlarm -> {
                viewModelScope.launch {
                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    coroutineScope.async {
                        repository.addAlarm(alarm = event.alarm)
                    }.await()
                    setEffect {
                        AlarmContract.AlarmEffect.ItemInserted(true, event.alarm)
                    }
                }
            }

            is AlarmContract.AlarmEvent.UpdateAlarm -> {
                viewModelScope.launch {
                    repository.updateAlarm(alarm = event.alarm)
                }
            }

            is AlarmContract.AlarmEvent.DeleteAlarm -> {
                viewModelScope.launch {
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