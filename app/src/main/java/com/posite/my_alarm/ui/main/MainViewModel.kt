package com.posite.my_alarm.ui.main

import androidx.lifecycle.viewModelScope
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.repository.TimeRepository
import com.posite.my_alarm.ui.alarm.RemainTime
import com.posite.my_alarm.ui.alarm.calculateRemainTime
import com.posite.my_alarm.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: TimeRepository) :
    BaseViewModel<MainContract.MainEvent, MainContract.MainUiState, MainContract.MainEffect>() {

    override fun createInitialState(): MainContract.MainUiState {
        return MainContract.MainUiState()
    }

    override fun handleEvent(event: MainContract.MainEvent) {
        when (event) {
            is MainContract.MainEvent.GetAlarmList -> {
                viewModelScope.launch {
                    repository.getAlarmStates().collect { alarms ->
                        setState { copy(alarmList = alarms) }
                        setState { copy(minTime = calculateRemainTime(alarms)) }
                    }
                }
            }

            is MainContract.MainEvent.GetAlarmState -> {
                viewModelScope.launch {
                    repository.getAlarmStateById(id = event.id).collect { result ->
                        result?.let { setState { copy(selectedAlarm = it) } }
                    }
                }
            }

            is MainContract.MainEvent.InsertAlarm -> {
                viewModelScope.launch {
                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    coroutineScope.async {
                        repository.addAlarm(alarm = event.alarm)
                    }.await()
                    setEffect {
                        MainContract.MainEffect.ItemInserted(true, event.alarm)
                    }
                }
            }

            is MainContract.MainEvent.UpdateAlarm -> {
                viewModelScope.launch {
                    repository.updateAlarm(alarm = event.alarm)
                }
            }

            is MainContract.MainEvent.DeleteAlarm -> {
                viewModelScope.launch {
                    repository.deleteAlarms(alarm = event.alarm)
                }
            }

            is MainContract.MainEvent.CalculateMinTime -> {
                if (event.minTime != null) {
                    setState { copy(minTime = event.minTime) }
                } else {
                    setState { copy(minTime = null) }
                }
            }
        }
    }


    fun getAlarmList() = setEvent(MainContract.MainEvent.GetAlarmList)

    fun getAlarmState(id: Long) = setEvent(MainContract.MainEvent.GetAlarmState(id))

    fun insertAlarmState(alarm: AlarmStateEntity) =
        setEvent(MainContract.MainEvent.InsertAlarm(alarm))

    fun updateAlarmState(alarm: AlarmStateEntity) =
        setEvent(MainContract.MainEvent.UpdateAlarm(alarm))

    fun deleteAlarmState(alarm: AlarmStateEntity) =
        setEvent(MainContract.MainEvent.DeleteAlarm(alarm))

    fun saveRemainTime(minTime: RemainTime?) =
        setEvent(MainContract.MainEvent.CalculateMinTime(minTime))

}