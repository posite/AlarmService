package com.posite.my_alarm.ui.lock

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.posite.my_alarm.data.repository.TimeRepository
import com.posite.my_alarm.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(private val repository: TimeRepository) :
    BaseViewModel<LockContract.LockEvent, LockContract.LockUiState, LockContract.LockEffect>() {

    override fun createInitialState(): LockContract.LockUiState {
        return LockContract.LockUiState()
    }

    override fun handleEvent(event: LockContract.LockEvent) {
        when (event) {
            is LockContract.LockEvent.GetAlarmState -> {
                viewModelScope.launch {
                    repository.getAlarmStateById(id = event.id).collect { result ->
                        Log.d("LockViewModel", "getAlarmStateById: $result")
                        if (result == null) {
                            setEffect { LockContract.LockEffect.ISAlarmNotExist }
                        } else {
                            setState { copy(alarm = result) }
                            setEffect { LockContract.LockEffect.ISAlarmExist(result) }
                        }
                    }
                }
            }
        }
    }

    fun getAlarmStateById(id: Long) = setEvent(LockContract.LockEvent.GetAlarmState(id))
}