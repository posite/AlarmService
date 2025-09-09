package com.posite.my_alarm

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
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
        context = applicationContext
        timeRepository = TimeRepository(alarmStateDao)
    }

    companion object {
        fun Context.getMyApplication(): AlarmApplication {
            return applicationContext as AlarmApplication
        }

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun getString(@StringRes stringResId: Int): String {
            return context.getString(stringResId)
        }
    }
}