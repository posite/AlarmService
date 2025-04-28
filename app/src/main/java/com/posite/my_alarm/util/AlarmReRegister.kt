package com.posite.my_alarm.util

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.posite.my_alarm.AlarmApplication.Companion.getMyApplication
import kotlinx.coroutines.flow.first

@HiltWorker
class AlarmReRegister(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val repository = applicationContext.getMyApplication().timeRepository
    override suspend fun doWork(): Result {
        try {
            val alarms = repository.getAlarmStates().first()
            alarms.forEach {
                AlarmScheduler.setExactAlarm(applicationContext, it)
                Log.d("AlarmReRegister", "Alarm set for ID: ${it.id}")
            }
        } catch (e: Exception) {
            Log.e("AlarmReRegister", "Error re-registering alarms: ${e.message}")
            return Result.failure()
        }

        return Result.success()
    }
}