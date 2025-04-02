package com.posite.my_alarm.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.posite.my_alarm.data.dao.AlarmStateDao
import com.posite.my_alarm.data.entity.AlarmStateEntity

@Database(entities = [AlarmStateEntity::class], version = 1)
abstract class AlarmStateDB : RoomDatabase() {
    abstract fun alarmStateDao(): AlarmStateDao
}