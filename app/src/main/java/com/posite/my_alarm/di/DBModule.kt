package com.posite.my_alarm.di

import android.content.Context
import androidx.room.Room
import com.posite.my_alarm.data.dao.AlarmStateDao
import com.posite.my_alarm.data.db.AlarmStateDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DBModule {
    @Singleton
    @Provides
    fun provideAlarmStateDatabase(
        @ApplicationContext context: Context
    ): AlarmStateDB = Room.databaseBuilder(
        context,
        AlarmStateDB::class.java,
        "alarm_db"
    ).fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideAlarmStateDao(alarmDB: AlarmStateDB): AlarmStateDao = alarmDB.alarmStateDao()
}