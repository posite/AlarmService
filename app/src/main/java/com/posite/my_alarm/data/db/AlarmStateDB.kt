package com.posite.my_alarm.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.posite.my_alarm.data.dao.AlarmStateDao
import com.posite.my_alarm.data.entity.AlarmStateEntity
import com.posite.my_alarm.data.entity.DayOfWeekConverter

@Database(
    entities = [AlarmStateEntity::class], version = 2,
    exportSchema = true
)
@TypeConverters(DayOfWeekConverter::class)
abstract class AlarmStateDB : RoomDatabase() {
    abstract fun alarmStateDao(): AlarmStateDao
}