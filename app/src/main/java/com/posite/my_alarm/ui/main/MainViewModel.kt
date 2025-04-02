package com.posite.my_alarm.ui.main

import androidx.lifecycle.viewModelScope
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.repository.TimeRepository
import com.posite.my_alarm.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

            is MainContract.MainEvent.InsertAlarmState -> {
                viewModelScope.launch {
                    repository.addTime(time = event.alarm)
                }
            }

            is MainContract.MainEvent.UpdateAlarmState -> {
                viewModelScope.launch {
                    repository.updateTime(alarm = event.alarm)
                }
            }

            is MainContract.MainEvent.DeleteAlarmState -> {
                viewModelScope.launch {
                    repository.deleteTime(alarm = event.alarm)
                    setEffect { MainContract.MainEffect.ShowToast("Alarm deleted") }
                }
            }
        }
    }


    fun getAlarmList() = setEvent(MainContract.MainEvent.GetAlarmList)

    fun getAlarmState(id: Long) = setEvent(MainContract.MainEvent.GetAlarmState(id))

    fun insertAlarmState(alarm: AlarmStateEntity) =
        setEvent(MainContract.MainEvent.InsertAlarmState(alarm))

    fun updateAlarmState(alarm: AlarmStateEntity) =
        setEvent(MainContract.MainEvent.UpdateAlarmState(alarm))

    fun deleteAlarmState(alarm: AlarmStateEntity) =
        setEvent(MainContract.MainEvent.DeleteAlarmState(alarm))

}