package com.posite.my_alarm.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.posite.my_alarm.data.repository.TimeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AlarmReRegister @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TimeRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        repository.getAlarmStates().collect { alarms ->
            alarms.forEach {
                AlarmScheduler.setExactAlarm(applicationContext, it)
            }
        }

        return Result.success()
    }
}