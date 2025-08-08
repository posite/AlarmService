package com.posite.my_alarm.di

import android.app.AlarmManager
import android.content.Context
import com.posite.my_alarm.util.AlarmSoundPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {
    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideMediaPlayer(@ApplicationContext context: Context): AlarmSoundPlayer =
        AlarmSoundPlayer(context)
}