package com.posite.my_alarm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AlarmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}