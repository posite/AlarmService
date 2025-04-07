package com.posite.my_alarm.ui.main

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
class MainViewModel @Inject constructor(private val repository: TimeRepository) :
    BaseViewModel<MainContract.MainEvent, MainContract.MainUiState, MainContract.MainEffect>() {

    override fun createInitialState(): MainContract.MainUiState {
        return MainContract.MainUiState()
    }

    override fun handleEvent(event: MainContract.MainEvent) {
        when (event) {
            is MainContract.MainEvent.GetAlarmList -> {
                viewModelScope.launch {
                    repository.getAlarmStates().collect {
                        setState { copy(alarmList = it) }
                    }
                }
            }

            is MainContract.MainEvent.GetAlarmState -> {
                viewModelScope.launch {
                    repository.getAlarmStateById(id = event.id).collect { alarm ->
                        setState { copy(selectedAlarm = alarm) }
                    }
                }
            }

            is MainContract.MainEvent.InsertAlarm -> {
                viewModelScope.launch {
                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    coroutineScope.async {
                        repository.addAlarm(alarm = event.alarm)
                    }.await()
                    setEffect { MainContract.MainEffect.ItemInserted(true) }
                }
            }

            is MainContract.MainEvent.UpdateAlarm -> {
                viewModelScope.launch {
                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    coroutineScope.async {
                        repository.updateAlarm(alarm = event.alarm)
                        setState { copy(selectedAlarm = event.alarm) }
                    }.await()
                    setEffect { MainContract.MainEffect.ItemUpdated(true) }
                }
            }

            is MainContract.MainEvent.DeleteAlarm -> {
                viewModelScope.launch {
                    repository.deleteAlarms(alarm = event.alarm)
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

}