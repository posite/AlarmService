package com.posite.my_alarm.data.repository

import com.posite.my_alarm.data.dao.AlarmStateDao
import com.posite.my_alarm.data.entity.AlarmStateEntity
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TimeRepository @Inject constructor(private val timeDao: AlarmStateDao) {
    fun getAlarmStates() = flow {
        timeDao.getAllAlarmStates().collect {
            emit(it)
        }
    }

    fun getAlarmStateById(id: Long) = flow {
        timeDao.getAlarmStateById(id).collect {
            emit(it)
        }
    }

    suspend fun addAlarm(time: AlarmStateEntity) = timeDao.insertAlarmState(time)

    suspend fun updateAlarm(alarm: AlarmStateEntity) = timeDao.updateAlarmState(alarm)

    suspend fun deleteAlarms(alarm: AlarmStateEntity) = timeDao.deleteAlarmState(alarm)

    suspend fun deleteAlarms(alarms: List<AlarmStateEntity>) = timeDao.deleteAlarmStates(alarms)
}