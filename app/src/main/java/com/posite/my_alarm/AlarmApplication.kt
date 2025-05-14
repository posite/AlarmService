package com.posite.my_alarm

import android.app.Application
import android.content.Context
import com.posite.my_alarm.data.dao.AlarmStateDao
import com.posite.my_alarm.data.repository.TimeRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AlarmApplication : Application() {
    lateinit var timeRepository: TimeRepository

    @Inject
    lateinit var alarmStateDao: AlarmStateDao

    override fun onCreate() {
        super.onCreate()
        timeRepository = TimeRepository(alarmStateDao)
    }

    companion object {
        fun Context.getMyApplication(): AlarmApplication {
            return applicationContext as AlarmApplication
        }
    }
}